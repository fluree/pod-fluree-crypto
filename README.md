# pod-fluree-crypto

This is a [Babashka](https://babashka.org) [pod](https://book.babashka.org/#pods)
for the [fluree.crypto library](https://github.com/fluree/fluree.crypto)

## Building

Pods are built as native binaries for their respective platforms, so this uses
GraalVM native-image. You need to install a Graal JDK and the native-image tool
and then run in this repo:

`clojure -M:native-image`

This should produce a binary named `pod-fluree-crypto` for your platform in the
project root.

## Usage

```clojure
(require '[babashka.pods :as pods])
(pods/load-pod "path/to/pod-fluree-crypto")
(require '[pod.fluree.crypto :as crypto])

(crypto/sha2-256 "foo") ; => "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae"
```

## License

Copyright (c) 2021 Fluree, PBC

This program and the accompanying materials are made
available under the terms of the Eclipse Public License 2.0
which is available at https://www.eclipse.org/legal/epl-2.0/

SPDX-License-Identifier: EPL-2.0
