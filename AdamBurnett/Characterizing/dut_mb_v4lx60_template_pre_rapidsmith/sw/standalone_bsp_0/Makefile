# Makefile generated by Xilinx SDK.
# Modified by AGS:

-include libgen.options

LIBRARIES = ${PROCESSOR}/lib/libxil.a
MSS = system.mss

libs: $(HWSPEC) $(LIBRARIES)

$(HWSPEC):
	make -C ../../hw sdk

$(LIBRARIES): $(MSS)
	libgen -hw ${HWSPEC}\
	       ${REPOSITORIES}\
	       -pe ${PROCESSOR} \
	       -log libgen.log \
	       $(MSS)
	touch $(LIBRARIES)

clean:
	rm -rf ${PROCESSOR}
	rm -rf libgen.log
	rm -rf .cproject
	rm -rf .project
	rm -rf .sdkproject
