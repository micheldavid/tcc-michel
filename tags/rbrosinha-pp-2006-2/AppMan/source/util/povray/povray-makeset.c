#include "stdio.h"
#include "stdlib.h"

/* 
	The povray file will be overf.pov 
	The ini files will be overf_nnnn.ini
	The resulting PPM file will be overf_nnnn.ppm
*/

int main(int argc,char **argv)
{
	int i;
	char fname[256];
	FILE *fptr;

	if(argc < 5)
	{
		printf("%s BASENAME N WIDTH HEIGHT\n");
		return -1;
	}
	
	char *BASENAME = argv[1];
	/* The number of machines */
	int N = atoi(argv[2]);

	/* The image dimensions */
	int HEIGHT = atoi(argv[4]);
	int WIDTH  = atoi(argv[3]);

	for (i=0;i<N;i++) {
		sprintf(fname,"%s_%d.ini",BASENAME,i);
		fptr = fopen(fname,"w");
		fprintf(fptr,"Input_File_Name=%s.pov\n",BASENAME);
		fprintf(fptr,"Output_File_Name=%s_%d.ppm\n",BASENAME,i);
		fprintf(fptr,"Output_File_Type=P\n");
		fprintf(fptr,"Height=%d\n",HEIGHT);
		fprintf(fptr,"Width=%d\n",WIDTH);
		fprintf(fptr,"Start_Row=%d\n",(i*HEIGHT)/N+1);
		fprintf(fptr,"End_Row=%d\n",((i+1)*HEIGHT)/N);
		fprintf(fptr,"Antialias=on\n");
		fprintf(fptr,"Antialias_Threshold=0.3\n");
		fprintf(fptr,"Verbose=off\n");
		fprintf(fptr,"Quality=9\n");
		fprintf(fptr,"Radiosity=off\n");
		fclose(fptr);
	}	

	return 0;
}
