# com.braintribe.asm
`btasm` sources are based on ASM. The code can be created (and updated) by executing the following commands:

	# specify ASM version
	ASM_VERSION=9.6

	# specify artifact src dir (and use relative path to ensure that commands are executed from artifact group root)
	BTASM_SRC_DIR=../com.braintribe.asm/btasm/src

	# delete sources
	rm -rf ${BTASM_SRC_DIR}
	mkdir ${BTASM_SRC_DIR}

	# for each required ASM library download, extract and delete
	for asmlib in asm asm-analysis asm-commons asm-tree asm-util; do
	    curl --silent --output ${BTASM_SRC_DIR}/${asmlib}.jar https://repo1.maven.org/maven2/org/ow2/asm/${asmlib}/${ASM_VERSION}/${asmlib}-${ASM_VERSION}-sources.jar
	    unzip -q -o -d ${BTASM_SRC_DIR} ${BTASM_SRC_DIR}/${asmlib}.jar
	    rm ${BTASM_SRC_DIR}/${asmlib}.jar
	done

	# rename folder org/objectweb to com/braintribe
	mv ${BTASM_SRC_DIR}/org ${BTASM_SRC_DIR}/com
	mv ${BTASM_SRC_DIR}/com/objectweb ${BTASM_SRC_DIR}/com/braintribe

	# replace in files package name org.objectweb with com.braintribe
	find ${BTASM_SRC_DIR} -type f -exec perl -p -i -e 's/org\.objectweb/com.braintribe/g' {} \;