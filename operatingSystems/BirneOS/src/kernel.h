#ifndef KERNEL_H
#define KERNEL_H

#define VGA_WIDTH 80
#define VGA_HEIGHT 20

#include <stdint.h>
#include <stddef.h>

#include "./idt/idt.h"
#include "./io/io.h"
#include "./memory/heap/kheap.h"
#include "./memory/paging/paging.h"
#include "./string/string.h"
#include "./disk/disk.h"
#include "./fs/pparser.h"
#include "./disk/streamer.h"

#define BIRNEOS_MAX_PATH 108

void kernel_main();
void print(const char* str);

#endif