#include "appman_rmswrapper_pbs_drmaa_SessionImpl.h"
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <strings.h>
#include <string.h>
#include <errno.h>
#include <pbs_error.h>
#include <pbs_ifl.h>

/* enum copiado do código sge */
enum {
   /* -------------- these are relevant to all sections ---------------- */
   DRMAAJ_ERRNO_SUCCESS = 0, /* Routine returned normally with success. */
   DRMAAJ_ERRNO_INTERNAL_ERROR, /* Unexpected or internal DRMAA error like memory allocation, system call failure, etc. */
   DRMAAJ_ERRNO_DRM_COMMUNICATION_FAILURE, /* Could not contact DRM system for this request. */
   DRMAAJ_ERRNO_AUTH_FAILURE, /* The specified request is not processed successfully due to authorization failure. */
   DRMAAJ_ERRNO_INVALID_ARGUMENT, /* The input value for an argument is invalid. */
   DRMAAJ_ERRNO_NO_ACTIVE_SESSION, /* Exit routine failed because there is no active session */
   DRMAAJ_ERRNO_NO_MEMORY, /* failed allocating memory */

   /* -------------- init and exit specific --------------- */
   DRMAAJ_ERRNO_INVALID_CONTACT_STRING, /* Initialization failed due to invalid contact string. */
   DRMAAJ_ERRNO_DEFAULT_CONTACT_STRING_ERROR, /* DRMAA could not use the default contact string to connect to DRM system. */
   DRMAAJ_ERRNO_DRMS_INIT_FAILED, /* Initialization failed due to failure to init DRM system. */
   DRMAAJ_ERRNO_ALREADY_ACTIVE_SESSION, /* Initialization failed due to existing DRMAA session. */
   DRMAAJ_ERRNO_DRMS_EXIT_ERROR, /* DRM system disengagement failed. */

   /* ---------------- job attributes specific -------------- */
   DRMAAJ_ERRNO_INVALID_ATTRIBUTE_FORMAT, /* The format for the job attribute value is invalid. */
   DRMAAJ_ERRNO_INVALID_ATTRIBUTE_VALUE, /* The value for the job attribute is invalid. */
   DRMAAJ_ERRNO_CONFLICTING_ATTRIBUTE_VALUES, /* The value of this attribute is conflicting with a previously set attributes. */

   /* --------------------- job submission specific -------------- */
   DRMAAJ_ERRNO_TRY_LATER, /* Could not pass job now to DRM system. A retry may succeed however (saturation). */
   DRMAAJ_ERRNO_DENIED_BY_DRM, /* The DRM system rejected the job. The job will never be accepted due to DRM configuration or job template settings. */

   /* ------------------------------- job control specific ---------------- */
   DRMAAJ_ERRNO_INVALID_JOB, /* The job specified by the 'jobid' does not exist. */
   DRMAAJ_ERRNO_RESUME_INCONSISTENT_STATE, /* The job has not been suspended. The RESUME request will not be processed. */
   DRMAAJ_ERRNO_SUSPEND_INCONSISTENT_STATE, /* The job has not been running, and it cannot be suspended. */
   DRMAAJ_ERRNO_HOLD_INCONSISTENT_STATE, /* The job cannot be moved to a HOLD state. */
   DRMAAJ_ERRNO_RELEASE_INCONSISTENT_STATE, /* The job is not in a HOLD state. */
   DRMAAJ_ERRNO_EXIT_TIMEOUT, /* We have encountered a time-out condition for drmaa_synchronize or drmaa_wait. */
   DRMAAJ_ERRNO_NO_RUSAGE, /* This error code is returned by drmaa_wait() when a job has finished but no rusage and stat data could be provided. */
   DRMAAJ_ERRNO_INVALID_JOB_TEMPLATE, /* This error code is returned when an invalid job template is passed to a function. */
   DRMAAJ_ERRNO_NULL_POINTER, /* This error code is used for NullPointerExceptions */
   DRMAAJ_ERRNO_BUFFER_OVERFLOW, /* This error code is used for ArrayIndexOutOfBoundsExceptions */
   
   DRMAAJ_NO_ERRNO
};

/* fcs auxiliares copiadas do código sge */
#define NO_EXCEPTION_CLASS "Unable to locate class, %s, for DRMAA error: %s: %s"
#define MAX_STRING_SIZE 8192

static void print_message_and_throw_exception(JNIEnv *env, int errnum, const char *format, ...);
static void throw_exception (JNIEnv *env, int errnum, const char *message);
static char *get_exception_class_name (int errnum);

/* fcs auxiliares implementadas */
static void print_attrl(struct attrl *attrl);
static void set_attr_attrl(struct attrl *attrl_, char *attr_name, char *attr_value);
static struct attrl *remove_attr_attrl(struct attrl *attrl_, char *attr_name);
static char *get_value_attrl(struct attrl *attrl_, char *attr_name);


int session_id = 0;
struct attrl* job_template = NULL;

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeControl
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeControl
  (JNIEnv *env, jobject object, jstring string, jint num) {
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeExit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeExit
  (JNIEnv *env, jobject object) {
		printf("\n>>> nativeExit\t>>> PBS ENTRANDO NA FUNCAO\n");

		if (session_id == 0) {
			print_message_and_throw_exception(env, DRMAAJ_ERRNO_DRMS_EXIT_ERROR, "ERRO ENCERRANDO -- NAO EXISTE UMA SESSÃO ATIVA", "job id");
			return;
		}

		int disconnect_return = pbs_disconnect(session_id);
		printf("\n>>> nativeExit\t>>> PBS DISCONNECT RETURN: %i\n", disconnect_return);
		
	
		if (disconnect_return == 0) {
			session_id = disconnect_return;
		} else {
			print_message_and_throw_exception(env, DRMAAJ_ERRNO_DRMS_EXIT_ERROR, "ERRO ENCERRANDO", "job id");
		}
		printf("\n>>> nativeExit\t>>> PBS SAINDO DA FUNCAO\n");
		fflush(stdout);

  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeGetContact
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeGetContact
  (JNIEnv *env, jobject object) {
  	return NULL;
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeGetDRMSInfo
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeGetDRMSInfo
  (JNIEnv *env, jobject object) {
  	return NULL;
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeGetJobProgramStatus
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeGetJobProgramStatus
  (JNIEnv *env, jobject object, jstring string) {
  	return 0;
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeInit
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeInit
  (JNIEnv *env, jobject object, jstring string) {

		if (session_id != 0) {
			print_message_and_throw_exception(env, DRMAAJ_ERRNO_DRMS_INIT_FAILED, "ERRO INICIALIZANDO -- JÁ EXISTE UMA SESSÃO ATIVA", "job id");
			return;
		}

		jboolean iscopy;
		char *str2 = NULL;

		if (string != NULL) {
			const char *str = (*env)->GetStringUTFChars(env, string, &iscopy);
			str2 = (char *)malloc(strlen(str)*sizeof(char));
			strcpy(str2, str);
			(*env)->ReleaseStringUTFChars(env, string, str);
		}
		printf("\n>>> nativeInit\t>>> Conectando ao servidor \"%s\"\n", str2);
		int connect_return = pbs_connect(str2);
		printf("\n>>> nativeInit\t>>> PBS CONNECT RETURN: %i\n", connect_return);
                fflush(stdout);
	
		if (connect_return <= 0) {
			printf("\n>>> nativeInit\t>>> Codigo de erro PBS: %i\n", pbs_errno);
			perror("\n>>> nativeInit\t>>> PERROR");
			print_message_and_throw_exception(env, DRMAAJ_ERRNO_DRMS_INIT_FAILED, "ERRO INICIALIZANDO", "job id");
			return;
		} else {
			session_id = connect_return;
		}
  
	}

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeRunBulkJobs
 * Signature: (IIII)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeRunBulkJobs
  (JNIEnv *env, jobject object, jint num1, jint num2, jint num3, jint num4) {
  	return NULL;
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeRunJob
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeRunJob
  (JNIEnv *env, jobject object, jint num) {
		
		//printf("\n>>> nativeRunJob <<<\n");
		//print_attrl(job_template);
		// NOME DO SCRIPT QUE SERA EXECUTADO
		char *script_name = get_value_attrl((struct attrl *)job_template, "drmaa_remote_command");
		job_template = remove_attr_attrl((struct attrl *)job_template, "drmaa_remote_command");
		//printf("\n>>> nativeRunJob\t>>> PBS SCRIPT NAME: %s", script_name);
		printf("\n>>> nativeRunJob\t>>> ATTRL APOS REMOVER \"drmaa_remote_command\"\n");
		print_attrl(job_template);

		// OUTROS ATRIBUTOS
		char *output_path = get_value_attrl((struct attrl *)job_template, "drmaa_output_path"); 
		if (output_path != NULL) {
			printf("\n>>> nativeRunJob\t>>> output_path: %s\n", output_path);
			set_attr_attrl((struct attrl *)job_template, (char *)ATTR_o, (char *)output_path);
			job_template = remove_attr_attrl((struct attrl *)job_template, "drmaa_output_path");
			set_attr_attrl((struct attrl *)job_template, (char *)ATTR_o, (char *)output_path);
		}
		//job_template = remove_attr_attrl((struct attrl *)job_template, (char *)ATTR_o);
		printf("\n>>> nativeRunJob\t>>> ATTRL LOGO ANTES DE SUBMETER\n");
		print_attrl(job_template);
		fflush(stdout);

		char *job_id = pbs_submit(session_id, (struct attropl *)job_template, script_name, NULL, NULL);
		printf("\n>>> nativeRunJob\t>>> PBS SUBMIT RETURN: %s\n", job_id);

		if (job_id == NULL) {
			printf("\n>>> nativeRunJob\t>>> ENDERECO JOB_ID IGUAL A NULL\n");	
		} else {
			//int runjob_return = pbs_runjob(session_id, job_id, NULL, NULL);
			//printf("\n>>> nativeRunJob\t>>> PBS RUNJOB RETURN: %i\n", runjob_return);
		}
		
		fflush(stdout);
		
		return((*env)->NewStringUTF(env, job_id));
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeSynchronize
 * Signature: ([Ljava/lang/String;JZ)V
 */
JNIEXPORT void JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeSynchronize
  (JNIEnv *env, jobject object, jobjectArray objectArray, jlong num, jboolean boolean) {
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeWait
 * Signature: (Ljava/lang/String;J)Lpbswrapper/drmaa/JobInfoImpl;
 */
JNIEXPORT jobject JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeWait
  (JNIEnv *env, jobject object, jstring string, jlong num) {
  	return NULL;
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeAllocateJobTemplate
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeAllocateJobTemplate
  (JNIEnv *env, jobject object) {
  	return 1;
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeSetAttributeValue
 * Signature: (ILjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeSetAttributeValue
  (JNIEnv *env, jobject object, jint job_id, jstring name_str, jstring value_str) {

		const char *name = NULL;
		const char *value = NULL;
	
		if (name_str == NULL) {
			print_message_and_throw_exception (env, DRMAAJ_ERRNO_NULL_POINTER, "MSG_JDRMAA_NULL_POINTER_S", "attribute name");
			return;
		}
	
		if (value_str == NULL) {
			print_message_and_throw_exception (env, DRMAAJ_ERRNO_NULL_POINTER, "MSG_JDRMAA_NULL_POINTER_S", "attribute value");
			return;
		}
	
		name = (*env)->GetStringUTFChars (env, name_str, NULL);
		value = (*env)->GetStringUTFChars (env, value_str, NULL);
	
		//printf("\n>>> nativeSetAttributeValue\t>>> NAME: %s; VALUE: %s;\n", name, value);

		set_attr_attrl((struct attrl *)job_template, (char *)name, (char *)value);

		//print_attrl(job_template);		
	
		(*env)->ReleaseStringUTFChars (env, name_str, name);
		(*env)->ReleaseStringUTFChars (env, value_str, value);

		//fflush(stdout);

		return;
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeSetAttributeValues
 * Signature: (ILjava/lang/String;[Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeSetAttributeValues
  (JNIEnv *env, jobject object, jint num, jstring string, jobjectArray objectArray) {
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeGetAttributeNames
 * Signature: (I)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeGetAttributeNames
  (JNIEnv *env, jobject object, jint num) {
  	return NULL;
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeGetAttribute
 * Signature: (ILjava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeGetAttribute
  (JNIEnv *env, jobject object, jint num, jstring string) {
  	return NULL;
  }

/*
 * Class:     appman_rmswrapper_pbs_drmaa_SessionImpl
 * Method:    nativeDeleteJobTemplate
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_appman_rmswrapper_pbs_drmaa_SessionImpl_nativeDeleteJobTemplate
  (JNIEnv *env, jobject object, jint num) {
  }





/**********************************

métodos auxiliares

**********************************/

static void print_message_and_throw_exception(JNIEnv *env, int errnum,
                                              const char *format, ...)
{
   char message[MAX_STRING_SIZE + 1];
   va_list ap;

   va_start(ap, format);

   if (format != NULL) {
      vsnprintf(message, MAX_STRING_SIZE, format, ap);
      throw_exception (env, errnum, message);
   }
   else {
      throw_exception (env, errnum, NULL);
   }
}

static void throw_exception (JNIEnv *env, int errnum, const char *message)
{
   jclass newExcCls = NULL;
   
   newExcCls = (*env)->FindClass(env, get_exception_class_name (errnum));

   /* If we can't find the exception class, throw a RuntimeException. */
   if (newExcCls == NULL) {
      char no_class_message[MAX_STRING_SIZE];
      
      /* If we can't find the right exception, default to something we
       * really expect to be able to find. */
      newExcCls = (*env)->FindClass(env, "java/lang/RuntimeException");
      
      /* If it's still not found, give up. */
      if (newExcCls == NULL) {
         fprintf (stderr, NO_EXCEPTION_CLASS,
                  get_exception_class_name (errnum), "drma_strerr",
                  message);
         
         /* This if-else structure should now dump the thread of control out at
          * the end of the method.  Not doing so is an error. */
      }
      /* Otherwise, throw the Runtime exception. */
      else {
         snprintf (no_class_message, MAX_STRING_SIZE, NO_EXCEPTION_CLASS,
                   get_exception_class_name (errnum), "drma_strerr",
                   message);

         /* Throw an exception saying we couldn't find the exception. */
         (*env)->ThrowNew(env, newExcCls, no_class_message);
      }
   }
   /* If we found the class, throw the new exception. */
   else {
      (*env)->ThrowNew(env, newExcCls, message);
   }
}

static char *get_exception_class_name (int errnum)
{
   switch (errnum) {
      case DRMAAJ_ERRNO_INTERNAL_ERROR:
         return "org/ggf/drmaa/InternalException";
      case DRMAAJ_ERRNO_DRM_COMMUNICATION_FAILURE:
         return "org/ggf/drmaa/DrmCommunicationException";
      case DRMAAJ_ERRNO_AUTH_FAILURE:
         return "org/ggf/drmaa/AuthorizationException";
      case DRMAAJ_ERRNO_INVALID_ARGUMENT:
         return "org/ggf/drmaa/InvalidArgumentException";
      case DRMAAJ_ERRNO_NO_ACTIVE_SESSION:
         return "org/ggf/drmaa/NoActiveSessionException";
      case DRMAAJ_ERRNO_NO_MEMORY:
         return "java/lang/OutOfMemoryError";
      case DRMAAJ_ERRNO_INVALID_CONTACT_STRING:
         return "org/ggf/drmaa/InvalidContactStringException";
      case DRMAAJ_ERRNO_DEFAULT_CONTACT_STRING_ERROR:
         return "org/ggf/drmaa/DefaultContactStringException";
      case DRMAAJ_ERRNO_DRMS_INIT_FAILED:
         return "org/ggf/drmaa/DrmsInitException";
      case DRMAAJ_ERRNO_ALREADY_ACTIVE_SESSION:
         return "org/ggf/drmaa/AlreadyActiveSessionException";
      case DRMAAJ_ERRNO_DRMS_EXIT_ERROR:
         return "org/ggf/drmaa/DrmsExitException";
      case DRMAAJ_ERRNO_INVALID_ATTRIBUTE_FORMAT:
         return "org/ggf/drmaa/InvalidAttributeFormatException";
      case DRMAAJ_ERRNO_INVALID_ATTRIBUTE_VALUE:
         return "org/ggf/drmaa/InvalidAttributeValueException";
      case DRMAAJ_ERRNO_CONFLICTING_ATTRIBUTE_VALUES:
         return "org/ggf/drmaa/ConflictingAttributeValuesException";
      case DRMAAJ_ERRNO_TRY_LATER:
         return "org/ggf/drmaa/TryLaterException";
      case DRMAAJ_ERRNO_DENIED_BY_DRM:
         return "org/ggf/drmaa/DeniedByDrmException";
      case DRMAAJ_ERRNO_INVALID_JOB:
         return "org/ggf/drmaa/InvalidJobException";
      case DRMAAJ_ERRNO_RESUME_INCONSISTENT_STATE:
         return "org/ggf/drmaa/ResumeInconsistentStateException";
      case DRMAAJ_ERRNO_SUSPEND_INCONSISTENT_STATE:
         return "org/ggf/drmaa/SuspendInconsistentStateException";
      case DRMAAJ_ERRNO_HOLD_INCONSISTENT_STATE:
         return "org/ggf/drmaa/HoldInconsistentStateException";
      case DRMAAJ_ERRNO_RELEASE_INCONSISTENT_STATE:
         return "org/ggf/drmaa/ReleaseInconsistentStateException";
      case DRMAAJ_ERRNO_EXIT_TIMEOUT:
         return "org/ggf/drmaa/ExitTimeoutException";
      case DRMAAJ_ERRNO_NO_RUSAGE:
         return "org/ggf/drmaa/NoResourceUsageException";
      case DRMAAJ_ERRNO_INVALID_JOB_TEMPLATE:
         return "org/ggf/drmaa/InvalidJobTemplateException";
      case DRMAAJ_ERRNO_NULL_POINTER:
         return "java/lang/NullPointerException";
      default:
         return "java/lang/RuntimeException";
   }
}

static void print_attrl(struct attrl *attrl_) {
		struct attrl *attr_tmp = attrl_;
		int i = 0;
		while (attr_tmp != NULL) {
			i++;
			printf("\n%i -> NAME: %s; VALUE: %s; NEXT: %x\n", i, attr_tmp->name, attr_tmp->value, attr_tmp->next);
			attr_tmp = attr_tmp->next;
		}
		return;
}

static char *get_value_attrl(struct attrl *attrl_, char *attr_name) {
		char *attr_value = NULL;
		struct attrl *attr_tmp = attrl_;
		while (attr_tmp != NULL) {
			if (strcmp(attr_tmp->name, attr_name) == 0) {
				attr_value = (char *)malloc(strlen(attr_tmp->value)*sizeof(char));
				strcpy(attr_value, attr_tmp->value);
			}
			attr_tmp = attr_tmp->next;
		}
		return attr_value;
}

static struct attrl *remove_attr_attrl(struct attrl *attrl_, char *attr_name) {
		struct attrl *attr_tmp = attrl_;
		struct attrl *prev_attr = NULL;
		int i = 0;
		while (attr_tmp != NULL) {
			printf("\t\t\t>>> %d --- %x --- %x \n", i++, &attr_tmp, &attrl_);
			if (strcmp(attr_tmp->name, attr_name) == 0) {
				if (prev_attr != NULL) {
					prev_attr->next = attr_tmp->next;
				} else if (attr_tmp->next == NULL) {
					attrl_ = NULL;
				} else {
					attrl_ = attr_tmp->next;
				}
			}
			prev_attr = attr_tmp;
			attr_tmp = attr_tmp->next;
		}
		printf("\t\t\t>>> %x +++ %x \n", &attr_tmp, &attrl_);
		fflush(stdout);
		
		return attrl_;
}

static void set_attr_attrl(struct attrl *attrl_, char *attr_name, char *attr_value) {
		struct attrl *attr_tmp = attrl_;
		struct attrl *last_attr = NULL;
		int new_attr_flag = 1;

		while (attr_tmp != NULL) {
			if (strcmp(attr_tmp->name, attr_name) == 0) {
				strcpy(attr_tmp->value, attr_value);
				new_attr_flag = 0;
			}
			last_attr = attr_tmp;
			attr_tmp = attr_tmp->next;
		}

		if (new_attr_flag) {
			struct attrl *new_attr = (struct attrl *)malloc(sizeof(struct attrl));
			new_attr->name = (char *)malloc(strlen(attr_name)*sizeof(char));
			new_attr->value = (char *)malloc(strlen(attr_value)*sizeof(char));
			strcpy(new_attr->name, attr_name);
			strcpy(new_attr->value, attr_value);

			new_attr->next = NULL;
			if (last_attr != NULL) {
				last_attr->next = new_attr;
			} else {
				job_template = new_attr;
			}
		}

		return;
}
