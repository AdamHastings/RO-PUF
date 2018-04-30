HWBASE=../../hw

PROCESSOR = microblaze_0
HWSPEC    = $(HWBASE)/SDK/SDK_Export/hw/system.xml
LIBRARIES = $(PROCESSOR)/lib/libxil.a
MSS       = system.mss

libs: $(HWSPEC) $(MSS) $(LIBRARIES)

#$(HWSPEC):
#	make -C $(HWBASE) sdk

$(LIBRARIES): $(MSS)
	libgen -hw $(HWSPEC)\
	       -pe $(PROCESSOR) \
	       -log libgen.log \
	       $(MSS)
	touch $(LIBRARIES)

$(MSS): $(HWSPEC)
	mkdir -p _appguru_temp_
	cd _appguru_temp_ ; \
	appguru -hw ../$(HWSPEC) -app empty_application -pe $(PROCESSOR) ; \
	cp $(MSS) ../$(MSS) ; \
	cd .. ; \
	rm -rf _appguru_temp_

appguru:
	mkdir -p _appguru_temp_
	cd _appguru_temp_ ; \
	appguru -hw ../$(HWSPEC) -app empty_application -pe $(PROCESSOR)

clean:
	rm -rf $(PROCESSOR)
	rm -rf libgen.log
	rm -rf .cproject
	rm -rf .project
	rm -rf .sdkproject
	rm -rf $(MSS)
