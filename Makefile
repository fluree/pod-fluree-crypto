SOURCES := $(shell find src)

pod-fluree-crypto: $(SOURCES) deps.edn
	clojure -M:native-image

.PHONY: clean

clean:
	rm -f pod-fluree-crypto
	rm -rf classes
