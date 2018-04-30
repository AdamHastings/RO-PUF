## Output ELF to run on Microblaze on V4LX60 Board
V4LX60_ELF=test_app.elf

## Source Directory for C File(s)
SRC_DIR = ./src

## Source for Microblaze Test Harness Application
C_SRC=$(SRC_DIR)/main.c

## Linker Script Source
V4LX60_LD_SRC=$(SRC_DIR)/main.ld

## Microblaze Device (Which Processor to use as Harness)
V4LX60_MB_DEV = microblaze_0

## Board Support Packages for V4LX60
V4LX60_BSP_DIR = ../standalone_bsp_0

## Library Paths for V4LX60
V4LX60_LIB_DIR = $(V4LX60_BSP_DIR)/$(V4LX60_MB_DEV)/lib

## Include Paths for V4LX60
V4LX60_INC_DIR = $(V4LX60_BSP_DIR)/$(V4LX60_MB_DEV)/include

## Endianness
ENDIANNESS=big-endian

V4LX60_ELFFLAGS = -Wl,--no-relax -Wl,-T -Wl,$(V4LX60_LD_SRC) -L$(V4LX60_LIB_DIR) -m$(ENDIANNESS) -mxl-barrel-shift -mxl-pattern-compare -mcpu=v8.30.a -mno-xl-soft-mul
V4LX60_OBJFLAGS = -Wall -O0 -g3 -c -fmessage-length=0 -Wl,--no-relax -I$(V4LX60_INC_DIR) -m$(ENDIANNESS) -mxl-barrel-shift -mxl-pattern-compare -mcpu=v8.30.a -mno-xl-soft-mul -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)"

V4LX60_DIR  = ./v4lx60
V4LX60_OBJS = $(C_SRC:$(SRC_DIR)/%.c=$(V4LX60_DIR)/%.o)

LIBS := -Wl,--start-group,-lxil,-lgcc,-lc,--end-group

CC=mb-gcc
RM=rm -rf

all: $(V4LX60_BSP_DIR)/$(V4LX60_MB_DEV) $(V4LX60_ELF)

$(V4LX60_BSP_DIR)/$(V4LX60_MB_DEV):
	make -C $(V4LX60_BSP_DIR)

$(V4LX60_ELF): DEVICE = $(V4LX60)
$(V4LX60_ELF): $(V4LX60_DIR) $(V4LX60_OBJS)
	@echo Building target: $@
	@echo Invoking: MicroBlaze gcc linker
	$(CC) $(CFLAGS) $(V4LX60_ELFFLAGS) -o $(V4LX60_ELF) $(V4LX60_OBJS) $(LIBS)
	@echo Finished building target: $@
	@echo ' '

$(V4LX60_DIR)/%.o: $(SRC_DIR)/%.c
	@echo Building file: $<
	@echo Invoking: MicroBlaze gcc compiler
	$(CC) $(CFLAGS) $(V4LX60_OBJFLAGS) -o"$@" "$<"
	@echo Finished building: $<
	@echo ' '

$(V4LX60_DIR):
	mkdir $(V4LX60_DIR)

clean:
	rm -rf _impactbatch.log
	rm -rf $(V4LX60_ELF)
	rm -rf $(V4LX60_DIR)
	rm -rf .cproject
	rm -rf .project