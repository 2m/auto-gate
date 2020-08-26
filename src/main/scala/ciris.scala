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

import ciris.ConfigDecoder
import sttp.model.Uri

object CirisDecoders {
  implicit val uriConfigDecoderConfigDecoder: ConfigDecoder[String, Uri] =
    ConfigDecoder.fromOption("sttp.model.Uri") { value =>
      Uri.safeApply(value).toOption
    }
}
