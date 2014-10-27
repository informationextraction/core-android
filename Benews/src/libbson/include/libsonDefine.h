/*
 * libsonCmd.h
 *
 *  Created on: Oct 21, 2014
 *      Author: zad
 */

#ifndef LIBSONCMD_H_
#define LIBSONCMD_H_



#define ELEMENT2PROCESS 4

#define TYPE_TEXT 1
#define TYPE_TEXT_DIR "text"

#define TYPE_AUDIO 2
#define TYPE_AUDIO_DIR "audio"

#define TYPE_VIDEO 3
#define TYPE_VIDEO_DIR "video"

#define TYPE_IMGL 4
#define TYPE_IMG_DIR "img"

#define TYPE_HTML 5
#define TYPE_HTML_DIR "html"

enum{
FRAGMENT_WILDCHAR =-1,
FRAGMENT_STRIP =(FRAGMENT_WILDCHAR-1),
FRAGMENT_VALID =FRAGMENT_STRIP,
};
#endif /* LIBSONCMD_H_ */
