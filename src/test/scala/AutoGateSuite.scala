/*
 * Copyright 2020 github.com/2m/auto-gate/contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lt.dvim.autogate

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Tcp
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.util.ByteString

import ciris.Secret
import munit.FunSuite
import sttp.client._

import lt.dvim.autogate.AutoGate._

class AutoGateSuite extends FunSuite with Fixtures {
  withTcpServer.test("sends a request") { server =>
    import server.sys.dispatcher
    openGate()(
      Config("twilio-sid", Secret("twilio-token"), uri"http://localhost:${server.port}", "from", "to", "instructions")
    )
    val expected = s"""|POST /2010-04-01/Accounts/twilio-sid/Calls.json HTTP/1.1
                       |Connection: Upgrade, HTTP2-Settings
                       |Content-Length: 32
                       |Host: localhost:${server.port}
                       |HTTP2-Settings: AAEAAEAAAAIAAAABAAMAAABkAAQBAAAAAAUAAEAA
                       |Upgrade: h2c
                       |User-Agent: Java-http-client/14.0.1
                       |Accept-Encoding: gzip, deflate
                       |Authorization: Basic dHdpbGlvLXNpZDp0d2lsaW8tdG9rZW4=
                       |Content-Type: application/x-www-form-urlencoded
                       |
                       |To=to&From=from&Url=instructions
                       |""".stripMargin
    server.request.map { obtained =>
      assertNoDiff(obtained, expected)
    }
  }
}

trait Fixtures { self: munit.FunSuite =>
  case class TcpServer(binding: ServerBinding, request: Future[String], port: Int, sys: ActorSystem)

  val withTcpServer = FunFixture.async[TcpServer](
    setup = { _ =>
      implicit val sys = ActorSystem()
      import sys.dispatcher
      val serverSink = Sink.fold[ByteString, ByteString](ByteString.empty)(_ ++ _)
      val serverFlow =
        Flow.fromSinkAndSourceMat(serverSink, Source.single(ByteString("HTTP/1.1 200 OK\r\n\r\n")))(Keep.left)
      val (binding, connection) = Tcp().bind("localhost", 0).toMat(Sink.head)(Keep.both).run()
      val result = connection.flatMap(_.handleWith(serverFlow)).map(_.utf8String)
      binding.map(binding => TcpServer(binding, result, binding.localAddress.getPort(), sys))
    },
    teardown = { server =>
      implicit val ec = munitExecutionContext
      for {
        _ <- server.binding.unbind()
        _ <- server.sys.terminate()
      } yield ()
    }
  )
}
