#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/types_c.h>
//#include <opencv/cv.hpp>

extern "C"
{
    JNIEXPORT jstring JNICALL
    Java_com_example_opencvsample_MainActivity_version(
            JNIEnv *env,
            jobject) {
        std::string version = cv::getVersionString();
        return env->NewStringUTF(version.c_str());
    }

    JNIEXPORT jbyteArray
    JNICALL Java_com_example_opencvsample_MainActivity_rgba2bgra
            (
                    JNIEnv *env,
                    jobject obj,
                    jint w,
                    jint h,
                    jbyteArray src
            ) {
        // Obtaining element row
        // Need to release at the end
        jbyte *p_src = env->GetByteArrayElements(src, NULL);
        if (p_src == NULL) {
            return NULL;
        }

        // Convert arrangement to cv::Mat
        cv::Mat m_src(h, w, CV_8UC4, (u_char *) p_src);
        //cv::Mat m_dst(h, w, CV_8UC4);
        cv::Mat m_dst(h, w, CV_8UC1);

        // OpenCV process
        GaussianBlur(m_src, m_src, cv::Size(3,3), 0, 0, cv::BORDER_DEFAULT);

        //cv::cvtColor(m_src, m_dst, CV_RGBA2BGRA);
        cv::cvtColor(m_src, m_dst, CV_RGBA2GRAY);

        int scale = 1;
        int delta = 0;
        int ddepth = CV_16S;

        // Generate grad_x and grad_y
        cv::Mat grad_x, grad_y, grad, grad_color;
        cv::Mat abs_grad_x, abs_grad_y;

        /// Gradient X
        //Scharr( src_gray, grad_x, ddepth, 1, 0, scale, delta, BORDER_DEFAULT );
        Sobel( m_dst, grad_x, ddepth, 1, 0, 3, scale, delta, cv::BORDER_DEFAULT );
        convertScaleAbs( grad_x, abs_grad_x );

        /// Gradient Y
        //Scharr( src_gray, grad_y, ddepth, 0, 1, scale, delta, BORDER_DEFAULT );
        Sobel( m_dst, grad_y, ddepth, 0, 1, 3, scale, delta, cv::BORDER_DEFAULT );
        convertScaleAbs( grad_y, abs_grad_y );

        /// Total Gradient (approximate)
        addWeighted( abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad );

        // Grad to Color
        cv::cvtColor(grad, grad_color, CV_GRAY2RGBA);

        // Pick out arrangement from cv::Mat
        //u_char *p_dst = m_dst.data;
        u_char *p_dst = grad_color.data;

        // Assign element for return value use
        jbyteArray dst = env->NewByteArray(w * h * 4);
        if (dst == NULL) {
            env->ReleaseByteArrayElements(src, p_src, 0);
            return NULL;
        }
        env->SetByteArrayRegion(dst, 0, w * h * 4, (jbyte *) p_dst);

        // release
        env->ReleaseByteArrayElements(src, p_src, 0);

        return dst;
    }
}