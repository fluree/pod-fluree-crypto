SOURCES := $(shell find src)

pod-fluree-crypto: $(SOURCES) deps.edn
	clojure -M:native-image
