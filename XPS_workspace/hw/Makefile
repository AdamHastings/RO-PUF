-include Makefile.src

## XPS Project Files
SYSTEM_UCF      = data/system.ucf
SYSTEM_MHS      = system.mhs
## Build Variables
DEVICE          = xc4vlx60ff668-11
XPS_HDL_LANG    = vhdl
INTSTYLE        = default
PLATGEN_OPTIONS = -p $(DEVICE) -lang $(XPS_HDL_LANG) -intstyle $(INTSTYLE) -msg __xps/ise/xmsgprops.lst
## Intermediate Files - Normal Flow
SYSTEM_NGC      = implementation/system.ngc
SYSTEM_NGD      = implementation/system.ngd
SYSTEM_MAP_NCD  = implementation/system_map.ncd
SYSTEM_PAR_NCD  = implementation/system_par.ncd
SYSTEM_BIT      = implementation/system.bit
DOWNLOAD_BIT    = implementation/download.bit
## Supplemental Build Files
SYSTEM_BD_BMM   = implementation/system_bd.bmm
## SDK Files
SDK_EXPORT_DIR  = SDK/SDK_Export/hw
SYSTEM_XML      = $(SDK_EXPORT_DIR)/system.xml
## Software Application to build into download.bit
APP_ELF         = ../sw/test_app/test_app.elf
APP_SRC         = ../sw/test_app/src/main.c

## Default prints out the menu
all: help

help:
	@echo " "
	@echo " Makefile Options for Building Design"
	@echo "  make sdk       - runs XSDK Export for Sw"
	@echo "  make syn       - runs Platgen/NGC Build"
	@echo "  make ngd       - runs NGD Build"
	@echo "  make map       - runs MAP"
	@echo "  make par       - runs PAR"
	@echo "  make bits      - generate system bitstream"
	@echo "  make init_bram - generate download bitstream"
	@echo "  make clean_hw  - clean XPS project"
	@echo "  make clean_sw  - clean software"
	@echo "  make clean     - clean hardware and software"
	@echo " "

sdk: $(SYSTEM_XML)
syn: $(SYSTEM_NGC)
ngd: $(SYSTEM_NGD)
map: $(SYSTEM_MAP_NCD)
par: $(SYSTEM_PAR_NCD)
bits: $(SYSTEM_BIT)
init_bram: $(DOWNLOAD_BIT)

## Generates XML file used by libgen for
## building software application
$(SYSTEM_XML): $(SYSTEM_MHS)
	@mkdir -p $(SDK_EXPORT_DIR)
	psf2Edward -inp system.xmp -exit_on_error -dont_add_loginfo -make_inst_lower -edwver 1.2 -xml $(SYSTEM_XML)
	xdsgen -inp system.xmp -report $(SDK_EXPORT_DIR)/system.html -make_docs_local

## Starting with MHS file runs platgen and XST
## to build system.ngc file
$(SYSTEM_NGC): $(ALL_SRC)
	platgen $(PLATGEN_OPTIONS) $(SYSTEM_MHS)
	cd synthesis ; \
	rm -rf xst ; \
	xst -ifn "system_xst.scr" -intstyle silent ; \
	rm -rf xst

## Run NGD Build after NGC Build completes
$(SYSTEM_NGD): $(SYSTEM_NGC) $(SYSTEM_UCF)
	cd implementation ; \
	ngdbuild -p $(DEVICE) -nt timestamp -bm system.bmm ../$(SYSTEM_NGC) -uc ../$(SYSTEM_UCF) ../$(SYSTEM_NGD)

## Run MAP after NGD Build
$(SYSTEM_MAP_NCD): $(SYSTEM_NGD)
	cd implementation ; \
	map -o ../$(SYSTEM_MAP_NCD) -w -pr b -ol high -timing -detail ../$(SYSTEM_NGD) system.pcf

## Run PAR after MAP
$(SYSTEM_PAR_NCD): $(SYSTEM_MAP_NCD)
	cd implementation ; \
	par -w -ol high ../$(SYSTEM_MAP_NCD) ../$(SYSTEM_PAR_NCD) system.pcf

## Run BitGen on Unmodified PAR NCD
$(SYSTEM_BIT): $(SYSTEM_PAR_NCD)
	cd implementation ; \
	bitgen -d -w -f ../etc/bitgen.ut ../$(SYSTEM_PAR_NCD) ../$(SYSTEM_BIT)

## Generate download.bit on Unmodified System.bit and Application
$(DOWNLOAD_BIT): $(SYSTEM_BIT) $(APP_ELF)
	cd implementation ; \
	data2mem -bm ../$(SYSTEM_BD_BMM) -p $(DEVICE) -bt ../$(SYSTEM_BIT) -bd ../$(APP_ELF) tag microblaze_0  -o b ../$(DOWNLOAD_BIT)

## Compile application
$(APP_ELF): $(SYSTEM_XML) $(APP_SRC)
	make -C ../sw

clean: clean_hw clean_sw clean_impact

clean_sw:
	rm -rf SDK
	make -C ../sw clean

clean_hw:
	rm -rf hdl
	rm -rf synthesis
	rm -rf implementation
	rm -rf clock_generator_0.log
	rm -rf platgen.log
	rm -rf platgen.opt
	rm -rf psf2Edward.log
	rm -rf xdsgen.log
	rm -rf _xmsgs
	rm -rf __xps
	rm -rf system_incl.make
	rm -rf system.make
	rm -rf system.log
	rm -rf _impact.*
	rm -rf minicom.log

clean_impact:
	rm -rf _impact.cmd
	rm -rf impact_impact.xwbt
	rm -rf _impact.log
	rm -rf impact.xsl
	rm -rf usage_statistics_webtalk.html
	rm -rf webtalk_impact.xml
	rm -rf webtalk.log
