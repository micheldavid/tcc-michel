/* $Id: dotneato.h 271 2006-11-29 01:39:33Z rbrosinha $ $Revision: 271 $ */
/* vim:set shiftwidth=4 ts=8: */

/**********************************************************
*      This software is part of the graphviz package      *
*                http://www.graphviz.org/                 *
*                                                         *
*            Copyright (c) 1994-2004 AT&T Corp.           *
*                and is licensed under the                *
*            Common Public License, Version 1.0           *
*                      by AT&T Corp.                      *
*                                                         *
*        Information and Software Systems Research        *
*              AT&T Research, Florham Park NJ             *
**********************************************************/

#ifndef DOTNEATO_H
#define DOTNEATO_H

/* FIXME - these shouldn't be needed */
#define ENABLE_CODEGENS 1
#define GD_RENDER 1

#include <dot.h>
#include <neato.h>
#include <circle.h>
#include <fdp.h>
#include <circo.h>

#ifdef __cplusplus
extern "C" {
#endif

    extern GVC_t *gvContext();

#ifdef __cplusplus
}
#endif
#endif
