## Process this file with automake to produce Makefile.in

SUBDIRS = common gvrender pack dotgen neatogen twopigen fdpgen circogen gvre_svg

pdfdir = $(pkgdatadir)/doc/pdf
pkgconfigdir = $(pkglibdir)/pkgconfig

linkedman = neato.1 twopi.1 fdp.1 circo.1
linkedpdf = neato.pdf twopi.pdf fdp.pdf circo.pdf

AM_CPPFLAGS = \
        -I$(top_srcdir) \
        -I$(top_srcdir)/dotneato/common \
        -I$(top_srcdir)/dotneato/gvrender \
	-I$(top_srcdir)/dotneato/dotgen \
	-I$(top_srcdir)/dotneato/neatogen \
	-I$(top_srcdir)/dotneato/twopigen \
	-I$(top_srcdir)/dotneato/fdpgen \
	-I$(top_srcdir)/dotneato/circogen \
	-I$(top_srcdir)/dotneato/pack \
	-I$(top_srcdir)/pathplan \
	-I$(top_srcdir)/graph \
	-I$(top_srcdir)/cdt

pkginclude_HEADERS = dotneato.h
pkglib_LTLIBRARIES = libdotneato.la
pkgconfig_DATA = libdotneato.pc
bin_SCRIPTS = dotneato-config
bin_PROGRAMS = dot neato twopi fdp circo
noinst_PROGRAMS = dotmemtest neatomemtest twopimemtest fdpmemtest
man_MANS = dot.1 dotneato-config.1
pdf_DATA = dot.pdf dotneato-config.pdf

libdotneato_la_SOURCES = dotneato.c
libdotneato_la_LIBADD = \
	$(top_builddir)/dotneato/common/libcommon.la \
	$(top_builddir)/dotneato/gvrender/libgvrender.la \
	$(top_builddir)/dotneato/dotgen/libdotgen.la \
	$(top_builddir)/dotneato/neatogen/libneatogen.la \
	$(top_builddir)/dotneato/twopigen/libtwopigen.la \
	$(top_builddir)/dotneato/fdpgen/libfdpgen.la \
	$(top_builddir)/dotneato/circogen/libcircogen.la

dot_SOURCES = dot.c
dot_LDADD = \
	$(top_builddir)/dotneato/common/libcommon.la \
	$(top_builddir)/dotneato/gvrender/libgvrender.la \
	$(top_builddir)/dotneato/dotgen/libdotgen.la

neato_SOURCES = neato.c
neato_LDADD = \
	$(top_builddir)/dotneato/common/libcommon.la \
	$(top_builddir)/dotneato/gvrender/libgvrender.la \
	$(top_builddir)/dotneato/neatogen/libneatogen.la

fdp_SOURCES = fdp.c
fdp_LDADD = \
	$(top_builddir)/dotneato/common/libcommon.la \
	$(top_builddir)/dotneato/gvrender/libgvrender.la \
	$(top_builddir)/dotneato/fdpgen/libfdpgen.la

circo_SOURCES = circo.c
circo_LDADD = \
	$(top_builddir)/dotneato/common/libcommon.la \
	$(top_builddir)/dotneato/gvrender/libgvrender.la \
	$(top_builddir)/dotneato/circogen/libcircogen.la

twopi_SOURCES = twopi.c
twopi_LDADD = \
	$(top_builddir)/dotneato/common/libcommon.la \
	$(top_builddir)/dotneato/gvrender/libgvrender.la \
	$(top_builddir)/dotneato/twopigen/libtwopigen.la

dotmemtest_SOURCES = dotmemtest.c
dotmemtest_LDADD = $(dot_LDADD)

neatomemtest_SOURCES = neatomemtest.c
neatomemtest_LDADD = $(neato_LDADD)

twopimemtest_SOURCES = twopimemtest.c
twopimemtest_LDADD = $(twopi_LDADD)

fdpmemtest_SOURCES = fdpmemtest.c
fdpmemtest_LDADD = $(fdp_LDADD)

install-data-hook:
	(cd $(DESTDIR)$(man1dir); for i in $(linkedman); do rm -f $$i; $(LN_S) dot.1 $$i; done;)
	(cd $(DESTDIR)$(pdfdir); for i in $(linkedpdf); do rm -f $$i; $(LN_S) dot.pdf $$i; done;)

#install-exec-hook:
#	(cd $(DESTDIR)$(bindir); for i in $(linkedprogram); do rm -f $$i; $(LN_S) dot $$i; done;)

uninstall-hook:
	(cd $(DESTDIR)$(man1dir); for i in $(linkedman); do rm -f $$i; done;)
	(cd $(DESTDIR)$(pdfdir); for i in $(linkedpdf); do rm -f $$i; done;)
#	(cd $(DESTDIR)$(bindir); for i in $(linkedprogram); do rm -f $$i; done;)

dot.pdf: $(top_srcdir)/dotneato/dot.1
	groff -Tps -man $(top_srcdir)/dotneato/dot.1 | ps2pdf - - >$@

dotneato-config.pdf: $(top_srcdir)/dotneato/dotneato-config.1
	groff -Tps -man $(top_srcdir)/dotneato/dotneato-config.1 | ps2pdf - - >$@

EXTRA_DIST = XFIG_FORMAT3.2.txt Makefile.old dot.1 dotneato-config.1 dot.pdf dotneato-config.pdf dotneato-config.in awk

DISTCLEANFILES = dot.pdf dotneato-config.pdf
