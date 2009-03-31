/* $Id: dot.c 271 2006-11-29 01:39:33Z rbrosinha $ $Revision: 271 $ */
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


/*
 * Written by Stephen North and Eleftherios Koutsofios.
 */

#include	"dot.h"
#ifdef HAVE_CONFIG_H
#include "config.h"
#endif
#include	<time.h>
#ifdef HAVE_UNISTD_H
#include	<unistd.h>
#endif

char *Info[] = {
    "dot",			/* Program */
    VERSION,			/* Version */
    BUILDDATE			/* Build Date */
};

static GVC_t *gvc;

#ifndef MSWIN32
static void intr(int s)
{
    if (gvc->g)
	dotneato_write(gvc);
    dotneato_terminate(gvc);
    exit(1);
}
#endif

int main(int argc, char **argv)
{
    graph_t *g, *prev = NULL;

    gvc = gvNEWcontext(Info, username());

    dotneato_initialize(gvc, argc, argv);
#ifndef MSWIN32
    signal(SIGUSR1, toggle);
    signal(SIGINT, intr);
#endif

    while ((g = next_input_graph())) {
	if (prev) {
	    dot_cleanup(prev);
	    agclose(prev);
	}
	prev = g;

	gvBindContext(gvc, g);

	dot_layout(g);
	dotneato_write(gvc);
    }
    dotneato_terminate(gvc);
    return 1;
}
