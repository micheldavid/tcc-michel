#include "stdio.h"
#include "stdlib.h"
#include "strings.h"
#include "math.h"

/*
	Combine a series of ppm files from POVRAY into a single PPM image.
	Assume the files each represent consecutive row chunks as generated
	by POVRAY. Note: POVRAY puts the total image size in the header when
	rendering an image by pieces.
	Assume a bunch of file with the names prefix_nnnn.ppm where nnnn is
	a %d counter from 0 up to the number of frames-1
*/

int main(int argc,char **argv)
{
	int i,j,c,file,w,h;
	int height=0,width=0,bits;
	int counter = 0;
	FILE *fptr;
	char header[32];
	char fname[256];

	if (argc < 3) {
		fprintf(stderr,"Usage: %s ppmfilemask n_ppm_files\n",argv[0]);
		exit(-1);
	}

	int n = atoi(argv[2]);

	for (counter=0; counter < n; counter++) {
		sprintf(fname,"%s_%d.ppm",argv[1],counter);

		/* Read the header */
		if ((fptr = fopen(fname,"r")) == NULL) {
			fprintf(stderr,"\tFailed to read \"%s\"\n",fname);
			break;
		}
		if (fscanf(fptr,"%s",header) != 1 || strstr(header,"P6") == NULL) {
			fprintf(stderr,"\tUnexpected header in \"%s\"\n",fname);
			exit(-1);
		}

		/* Read the width and height */
		if (fscanf(fptr,"%d %d",&w,&h) != 2) {
			fprintf(stderr,"\tUnexpected height and width in \"%s\"\n",fname);
			exit(-1);
		}
		if (width == 0)
			width = w;
		if (width != w) {
			fprintf(stderr,"\tAll image widths must be the same!\n");
			exit(-1);
		}
		height += h;

		/* Read the number of bits per pixel */
		if (fscanf(fptr,"%d",&bits) != 1 || bits != 255) {
			fprintf(stderr,"\tUnexpected number of bits in \"%s\"\n",fname);
			exit(-1);
		}

		/* Skip to the binary data */
		while ((c = fgetc(fptr)) != '\n' && c != EOF)
			;

		/* Write the header for the first file */
		if (counter == 0) {
			printf("P6\n");
			printf("%d %d\n255\n",width,height);
		}

		/* Write the binary for the all the files */
		while ((c = fgetc(fptr)) != EOF) 
			putchar(c);

		fclose(fptr);
	}

	printf("The image dimensions are %d x %d\n",width,height);
	return 0;
}

