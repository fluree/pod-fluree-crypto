FROM clojure:tools-deps-1.10.3.1075-slim-bullseye AS clojure

FROM ghcr.io/graalvm/native-image:ol8-java11-22.0.0.2 AS graalvm

# Oracle your yum repo is broke (last tested 2022-02-07 by WSM)
RUN sed -i -e 's/enabled=1/enabled=0/' /etc/yum.repos.d/ol8_graalvm_community.repo

RUN microdnf install git make findutils && microdnf clean all

COPY --from=clojure /usr/local/bin/clojure /usr/local/bin/clojure
COPY --from=clojure /usr/local/lib/clojure /usr/local/lib/clojure

RUN mkdir -p /pod-fluree-crypto
WORKDIR /pod-fluree-crypto

COPY deps.edn .
RUN clojure -A:native-image -P

COPY . .

ENTRYPOINT [""]
