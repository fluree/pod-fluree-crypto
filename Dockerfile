FROM clojure:tools-deps-1.10.3.1075-slim-bullseye AS clojure

FROM ghcr.io/graalvm/graalvm-ce:ol8-java11-22.0.0.2 AS graalvm

# Oracle your yum repo is broke (last tested 2022-02-07 by WSM)
# only exists in native-image image
#RUN rm /etc/yum.repos.d/ol8_graalvm_community.repo

# can't use the official native-image image until they fix this:
# https://github.com/graalvm/container/issues/49
RUN gu install native-image

RUN microdnf install git make findutils && microdnf clean all

COPY --from=clojure /usr/local/bin/clojure /usr/local/bin/clojure
COPY --from=clojure /usr/local/lib/clojure /usr/local/lib/clojure

RUN mkdir -p /pod-fluree-crypto
WORKDIR /pod-fluree-crypto

COPY deps.edn .
RUN clojure -A:native-image -P

COPY . .

ENTRYPOINT [""]
