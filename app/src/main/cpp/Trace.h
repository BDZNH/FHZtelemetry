//
// Created by BDZNH on 2024/4/5.
//

#ifndef MY_APPLICATION_TRACE_H
#define MY_APPLICATION_TRACE_H
#include <android/trace.h>
class ScopedTrace{
public:
    ScopedTrace(const char* name){
        ATrace_beginSection(name);
    }
    ~ScopedTrace(){
        ATrace_endSection();
    }
};
#endif //MY_APPLICATION_TRACE_H
