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

#define HASH_FIELDS 5
#define HASH_FIELD_TYPE "type"
#define HASH_FIELD_PATH "path"
#define HASH_FIELD_TITLE "title"
#define HASH_FIELD_DATE "date"
#define HASH_FIELD_HEADLINE "headline"
enum{
FRAGMENT_WILDCHAR =-1,
FRAGMENT_STRIP =(FRAGMENT_WILDCHAR-1),
FRAGMENT_VALID =FRAGMENT_STRIP,
};
#endif /* LIBSONCMD_H_ */
