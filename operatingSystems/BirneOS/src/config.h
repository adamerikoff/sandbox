#ifndef CONFIG_H
#define CONFIG_H

#define KERNEL_CODE_SELECTOR 0x08
#define KERNEL_DATA_SELECTOR 0x10

#define BIRNEOS_TOTAL_INTERRUPTS 512

// 100 mb sized heap
#define BIRNEOS_HEAP_SIZE_BYTES 104857600
#define BIRNEOS_HEAP_BLOCK_SIZE 4096

#define BIRNEOS_HEAP_ADDRESS 0x01000000
#define BIRNEOS_HEAP_TABLE_ADDRESS 0x00007E00

#define BIRNEOS_SECTOR_SIZE 512

#define BIRNEOS_MAX_FILESYSTEMS 12
#define BIRNEOS_MAX_FILE_DESCRIPTORS 512

#define PEACHOS_MAX_PATH 108

#endif