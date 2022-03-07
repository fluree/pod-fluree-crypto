FROM clojure:tools-deps-1.10.3.1087-slim-bullseye AS clojure

FROM ghcr.io/graalvm/native-image:ol8-java11-22.0.0.2 AS graalvm

RUN microdnf install git make findutils && microdnf clean all

COPY --from=clojure /usr/local/bin/clojure /usr/local/bin/clojure
COPY --from=clojure /usr/local/lib/clojure /usr/local/lib/clojure

RUN mkdir -p /pod-fluree-crypto
WORKDIR /pod-fluree-crypto

COPY deps.edn .
RUN clojure -A:native-image -P

COPY . .

ENTRYPOINT [""]
