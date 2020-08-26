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
import sttp.client._
import sttp.client.httpclient.HttpClientSyncBackend
import sttp.tapir._
import sttp.tapir.client.sttp._
import sttp.tapir.model._

object AutoGate {
  val makeCall =
    endpoint.post
      .in(auth.basic[UsernamePassword])
      .in("2010-04-01" / "Accounts")
      .in(path[String]("accountId"))
      .in("Calls.json")
      .in(formBody[Map[String, String]])
      .out(stringBody)

  val rootUri = uri"https://api.twilio.com"

  case class Config(twilioSid: String, twilioToken: Secret[String], from: String, to: String, instructions: String)
  final val config = {
    val hocon = hoconAt("auto-gate")
    loadConfig(
      hocon[String]("twilio-sid"),
      hocon[Secret[String]]("twilio-token"),
      hocon[String]("from"),
      hocon[String]("to"),
      hocon[String]("instructions")
    )(Config.apply).orThrow()
  }

  val logger = Logger.getLogger(this.getClass().getName())
  implicit val backend = HttpClientSyncBackend()

  def openGate() = {
    val auth = UsernamePassword(config.twilioSid, Some(config.twilioToken.value))
    val form = Map("To" -> config.to, "From" -> config.from, "Url" -> config.instructions)
    val response =
      makeCall
        .toSttpRequest(rootUri)
        .apply((auth, config.twilioSid, form))
        .send()
    logger.info(s"Received status=${response.code} body=${response.body} from Twilio")
  }

  def main(args: Array[String]): Unit = openGate()
}

class AutoGate extends RawBackgroundFunction {
  import AutoGate._

  override def accept(json: String, context: Context): Unit = openGate()
}
