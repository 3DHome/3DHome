/*
    Copyright 2011 Google Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */


#ifndef SkGPipe_DEFINED
#define SkGPipe_DEFINED

#include "SkWriter32.h"
#include "SkFlattenable.h"

class SkCanvas;

class SkGPipeReader {
public:
    SkGPipeReader(SkCanvas* target);
    ~SkGPipeReader();

    enum Status {
        kDone_Status,   //!< no more data expected from reader
        kEOF_Status,    //!< need more data from reader
        kError_Status   //!< encountered error
    };

    // data must be 4-byte aligned
    // length must be a multiple of 4
    Status playback(const void* data, size_t length, size_t* bytesRead = NULL);

private:
    SkCanvas*           fCanvas;
    class SkGPipeState* fState;
};

///////////////////////////////////////////////////////////////////////////////

class SkGPipeController {
public:
    /**
     *  Called periodically by the writer, to get a working buffer of RAM to
     *  write into. The actual size of the block is also returned, and must be
     *  actual >= minRequest. If NULL is returned, then actual is ignored and
     *  writing will stop.
     *
     *  The returned block must be 4-byte aligned, and actual must be a
     *  multiple of 4.
     *  minRequest will always be a multiple of 4.
     */
    virtual void* requestBlock(size_t minRequest, size_t* actual) = 0;

    /**
     *  This is called each time some atomic portion of the data has been
     *  written to the block (most recently returned by requestBlock()).
     *  If bytes == 0, then the writer has finished.
     *
     *  bytes will always be a multiple of 4.
     */
    virtual void notifyWritten(size_t bytes) = 0;
};

class SkGPipeWriter {
public:
    SkGPipeWriter();
    ~SkGPipeWriter();

    bool isRecording() const { return NULL != fCanvas; }

    enum Flags {
        kCrossProcess_Flag = 1 << 0,
    };

    SkCanvas* startRecording(SkGPipeController*, uint32_t flags = 0);

    // called in destructor, but can be called sooner once you know there
    // should be no more drawing calls made into the recording canvas.
    void endRecording();

private:
    class SkGPipeCanvas* fCanvas;
    SkGPipeController*   fController;
    SkFactorySet         fFactorySet;
    SkWriter32 fWriter;
};

#endif
