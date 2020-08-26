# [auto-gate][] [![ci-badge][]][ci] [![gitter-badge][]][gitter]

[auto-gate]:    https://github.com/2m/auto-gate
[ci]:           https://github.com/2m/auto-gate/actions
[ci-badge]:     https://github.com/2m/auto-gate/workflows/ci/badge.svg
[gitter]:       https://gitter.im/2m/general
[gitter-badge]: https://badges.gitter.im/2m/general.svg

Auto Gate is a Google Cloud function that uses Twilio API to open a physical gate.

## Features

* triggers whenever a [Firestore document][firestore-trigger] under a specified path changes
* users [tapir][] for [describing Twilio endpoint](twilio-endpoint)

[firestore-trigger]: https://cloud.google.com/functions/docs/calling/cloud-firestore#event_types
[tapir]:             https://tapir.softwaremill.com/en/latest/
[twilio-endpoint]:   https://github.com/2m/auto-gate/blob/d1cfee220e9e4c9617d7c94baee5e135d514a811/src/main/scala/AutoGate.scala#L34-L40
