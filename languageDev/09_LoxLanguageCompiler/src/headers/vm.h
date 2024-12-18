#ifndef CLOX_VM_H
#define CLOX_VM_H

#include "object.h"
#include "chunk.h"
#include "value.h"
#include "settings.h"
#include "table.h"

#define STACK_DEFAULT 256

#ifdef CLOX_VARIABLE_STACK
#define STACK_POSITION(vm) \
    (size_t)((vm->stackTop)-(vm->stack))
#define STACK_CAPACITY(vm) \
    (size_t)((vm->stackMax)-(vm->stack))
#endif

typedef struct {
    Chunk* chunk;
    uint8_t* ip;
    Value* stackTop;
    Table globals;
#ifdef CLOX_CONST_KEYWORD
    Table constGlobals;
#endif
    Table strings;
    FreeList freeList;
#ifdef CLOX_VARIABLE_STACK
    Value* stack;
    Value* stackMax;
#else
    Value stack[STACK_DEFAULT];
#endif
} VM;

typedef enum {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR
} InterpretResult;

void initVM(VM *vm);
void freeVM(VM *vm);
InterpretResult interpret(VM *vm, const char* source);
void push(VM *vm, Value value);
Value pop(VM *vm);

#endif
