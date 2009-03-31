/* $Id: neato.c 271 2006-11-29 01:39:33Z rbrosinha $ $Revision: 271 $ */
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

#include	"neato.h"
#ifdef HAVE_CONFIG_H
#include "config.h"
#endif
#include	<time.h>
#ifdef HAVE_UNISTD_H
#include	<unistd.h>
#endif

#if defined(HAVE_FENV_H) && defined(HAVE_FEENABLEEXCEPT)
/* __USE_GNU is needed for feenableexcept to be defined in fenv.h on GNU
 * systems.   Presumably it will do no harm on other systems. */
#define __USE_GNU
# include <fenv.h>
#elif HAVE_FPU_CONTROL_H
# include <fpu_control.h>
#elif HAVE_SYS_FPU_H
# include <sys/fpu.h>
#endif

char *Info[] = {
    "neato",			/* Program */
    VERSION,			/* Version */
    BUILDDATE			/* Build Date */
};

static GVC_t *gvc;

#ifndef MSWIN32
static void intr(int s)
{
    Agnode_t *n;

    if (gvc->g) {
	for (n = agfstnode(gvc->g); n; n = agnxtnode(gvc->g, n)) {
	    ND_coord_i(n).x = POINTS(ND_pos(n)[0]);
	    ND_coord_i(n).y = POINTS(ND_pos(n)[1]);
	}
	dotneato_write(gvc);
    }
    dotneato_terminate(gvc);
    exit(1);
}
static void fperr(int s)
{
    fprintf(stderr, "caught SIGFPE %d\n", s);
    /* signal (s, SIG_DFL); raise (s); */
    exit(1);
}
static void fpinit()
{
#if defined(HAVE_FENV_H) && defined(HAVE_FEENABLEEXCEPT)
    int exc = 0;
# ifdef FE_DIVBYZERO
    exc |= FE_DIVBYZERO;
# endif
# ifdef FE_OVERFLOW
    exc |= FE_OVERFLOW;
# endif
# ifdef FE_INVALID
    exc |= FE_INVALID;
# endif
    feenableexcept(exc);
#elif  HAVE_FPU_CONTROL_H
    /* On s390-ibm-linux, the header exists, but the definitions
     * of the masks do not.  I assume this is temporary, but until
     * there's a real implementation, it's probably safest to not
     * adjust the FPU on this platform.
     */
# if defined(_FPU_MASK_IM) && defined(_FPU_MASK_DM) && defined(_FPU_MASK_ZM) && defined(_FPU_GETCW)
    fpu_control_t fpe_flags = 0;
    _FPU_GETCW(fpe_flags);
    fpe_flags &= ~_FPU_MASK_IM;	/* invalid operation */
    fpe_flags &= ~_FPU_MASK_DM;	/* denormalized operand */
    fpe_flags &= ~_FPU_MASK_ZM;	/* zero-divide */
    /*fpe_flags &= ~_FPU_MASK_OM;        overflow */
    /*fpe_flags &= ~_FPU_MASK_UM;        underflow */
    /*fpe_flags &= ~_FPU_MASK_PM;        precision (inexact result) */
    _FPU_SETCW(fpe_flags);
# endif
#endif
    signal(SIGFPE, fperr);
}
#endif

static char *neatoFlags = "[-x] [-n<v>] ";
static char *neatoItems = "\
 -n[v]       - No layout mode 'v' (=1)\n\
 -x          - Reduce graph\n";

static int neatoArgs(int argc, char **argv)
{
    char **p = argv + 1;
    int i;
    char *arg;
    int cnt = 1;

    specificFlags = neatoFlags;
    specificItems = neatoItems;
    setCmdName(argv[0]);
    for (i = 1; i < argc; i++) {
	arg = argv[i];
	if (*arg == '-') {
	    switch (arg[1]) {
	    case 'x':
		Reduce = TRUE;
		break;
	    case 'n':
		if (arg[2]) {
		    Nop = atoi(arg + 2);
		    if (Nop <= 0) {
			fprintf(stderr,
				"Invalid parameter \"%s\" for -n flag\n",
				arg + 2);
			dotneato_usage(1);
		    }
		} else
		    Nop = 1;
		break;
	    default:
		cnt++;
		if (*p != arg)
		    *p = arg;
		p++;
		break;
	    }
	} else {
	    cnt++;
	    if (*p != arg)
		*p = arg;
	    p++;
	}
    }
    *p = 0;
    return cnt;
}

int main(int argc, char **argv)
{
    graph_t *g, *prev = NULL;

    gvc = gvNEWcontext(Info, username());

    argc = neatoArgs(argc, argv);
    dotneato_initialize(gvc, argc, argv);
#ifndef MSWIN32
    signal(SIGUSR1, toggle);
    signal(SIGINT, intr);
    fpinit();
#endif

    while ((g = next_input_graph())) {
	if (prev) {
	    neato_cleanup(prev);
	    agclose(prev);
	}
	prev = g;

	gvBindContext(gvc, g);

	neato_layout(g);
	dotneato_write(gvc);
    }
    dotneato_terminate(gvc);
    return 1;
}
