FROM clojure:openjdk-11-tools-deps-1.11.1.1113-slim-bullseye AS clojure

FROM ghcr.io/graalvm/native-image:ol8-java11-22.1.0 AS graalvm

RUN microdnf install git make findutils && microdnf clean all

COPY --from=clojure /usr/local/bin/clojure /usr/local/bin/clojure
COPY --from=clojure /usr/local/lib/clojure /usr/local/lib/clojure

RUN mkdir -p /pod-fluree-crypto
WORKDIR /pod-fluree-crypto

COPY deps.edn .
RUN clojure -A:native-image -P

COPY . .

ENTRYPOINT [""]
