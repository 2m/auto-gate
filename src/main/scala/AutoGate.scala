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

import java.util.logging.Logger

import ciris._
import com.google.cloud.functions.{Context, RawBackgroundFunction}
import lt.dvim.ciris.Hocon._
import sttp.client3.httpclient.HttpClientSyncBackend
import sttp.model.Uri
import sttp.tapir._
import sttp.tapir.client.sttp._
import sttp.tapir.generic.Configuration
import sttp.tapir.generic.auto._
import sttp.tapir.model._

import lt.dvim.autogate.CirisDecoders._

object AutoGate {
  case class Request(to: String, from: String, url: String)
  implicit val requestConfiguration: Configuration = Configuration(_.capitalize, None)

  val makeCall =
    endpoint.post
      .in(auth.basic[UsernamePassword]())
      .in("2010-04-01" / "Accounts")
      .in(path[String]("accountId"))
      .in("Calls.json")
      .in(formBody[Request])
      .out(stringBody)

  case class Config(
      twilioSid: String,
      twilioToken: Secret[String],
      twilioUri: Uri,
      from: String,
      to: String,
      instructions: String
  )
  def config = {
    val hocon = hoconAt("auto-gate")
    loadConfig(
      hocon[String]("twilio-sid"),
      hocon[Secret[String]]("twilio-token"),
      hocon[Uri]("twilio-uri"),
      hocon[String]("from"),
      hocon[String]("to"),
      hocon[String]("instructions")
    )(Config.apply).orThrow()
  }

  val logger = Logger.getLogger(this.getClass().getName())
  val backend = HttpClientSyncBackend()

  def openGate()(implicit config: Config) = {
    val auth = UsernamePassword(config.twilioSid, Some(config.twilioToken.value))
    val form = Request(config.to, config.from, config.instructions)
    val response =
      SttpClientInterpreter
        .toRequest(makeCall, config.twilioUri)
        .apply((auth, config.twilioSid, form))
        .header("User-Agent", "2m/auto-gate")
        .send(backend)
    logger.info(s"Received status=${response.code} body=${response.body} from Twilio")
  }

  def main(args: Array[String]): Unit = openGate()(config)
}

class AutoGate extends RawBackgroundFunction {
  import AutoGate._

  override def accept(json: String, context: Context): Unit = openGate()(config)
}
