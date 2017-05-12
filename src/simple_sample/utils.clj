(ns simple-sample.utils (:import [org.opencv.core
                    Mat Core Point Size MatOfDouble MatOfInt MatOfByte MatOfRect Scalar]
                   [org.opencv.imgcodecs Imgcodecs]
                   [org.opencv.imgproc Imgproc]
                   [java.awt.image BufferedImage]
		               [javax.swing ImageIcon]
                   )
          (:require [simple-sample.utils :as ssu]
                    [simple-sample.Imgproc :as ssI]
                    [simple-sample.Core :as ssC]))

;
(use 'seesaw.core)

(native!) ;this will tell to seesaw to make thing native for the OS, for example thing for MAC OS


(defn to-buffered-image [mat]
  (let [out (Mat.)
        colour? (< 1 (.channels mat))
        type (if colour?
               java.awt.image.BufferedImage/TYPE_3BYTE_BGR
               java.awt.image.BufferedImage/TYPE_BYTE_GRAY)
        width (.cols mat)
        height (.rows mat)]
    (do
      (if colour?
        (Imgproc/cvtColor mat out Imgproc/COLOR_BGR2RGB)
        (.copyTo mat out))
      (let [blen (* (.channels mat) width height)
            bytes (byte-array blen)]
        (.get out 0 0 bytes)
        (let [image (java.awt.image.BufferedImage. width height type)
              raster (.getRaster image)]
          (.setDataElements raster 0 0 width height bytes)
          image)))))

;

(defn show-mat
  "pass a mat as argument and show it in a windows"
  [mat]
  (let [f (frame :title "mat" :size [(.cols mat) :by (.rows mat)])
        lbl (label :icon (ImageIcon. (to-buffered-image mat)))
        _ (config! f :content lbl)]
    (-> f pack! show!)))

;
(defn resize
  "takes a matrix and an integer (if you want a square matrix)
  or a matrix and two integer.
  It returns a new matrix with the new dimension"
  ([mat n]
   (let [new_mat (Mat.)
         _ (Imgproc/resize mat new_mat (Size. n n))]
     new_mat))
  ([mat n m]
   (let [new_mat (Mat.)
         _ (Imgproc/resize mat new_mat (Size. n m))]
     new_mat)))

;

;(Core/inRange mat (Scalar. 0 255 100) (Scalar. 255 255 255) new-mat)
(defn test-HSV
  "only a temporary function to make some test about HSV property of an image
  IT HAS A BIG PROBLEM WHEN LOWER LIMIT IS SETTLED TO 0"
  [mat]
  (let [old-mat (ssI/toHSV mat)
        f (frame :title "mat" :size [100 :by 100])
        sldr-ll (slider :orientation :vertical :max 255 :value 0) ;;slider of lower limit
        sldr-ul (slider :orientation :vertical :max 255 :value 255) ;;slider of upper limit
        lbl (label :icon (ImageIcon. (to-buffered-image mat)))
        new-mat (Mat.)
        lower-limit (fn [down] (Scalar. down 0 0))
        upper-limit (fn [upp] (Scalar. upp 255 255))
        _ (listen sldr-ll
                  :mouse-released
                  (fn [e] (do (Core/inRange
                               old-mat
                               (lower-limit (value e))
                               (upper-limit (value sldr-ul))
                               new-mat)
                              (config! lbl :icon (ImageIcon. (to-buffered-image new-mat)))
                              (println "value of lower limit: " (value e)))))
        _ (listen sldr-ul
                  :mouse-released
                  (fn [e] (do (Core/inRange
                               old-mat
                               (lower-limit (value sldr-ll))
                               (upper-limit (value e))
                               new-mat)
                              (config! lbl :icon (ImageIcon. (to-buffered-image new-mat)))
                              (println "value of upper limit: " (value e)))))
        _ (config! f :content (horizontal-panel :items [sldr-ll sldr-ul lbl]))]
    (-> f pack! show!)))
;

(defn bitwise-or
  [mat1 mat2 & mat-list]
  (let [new-mat (Mat.)
        _ (Core/bitwise_or mat1 mat2 new-mat)]
    (if-not (empty? mat-list) (apply bitwise-or new-mat (first mat-list) (rest mat-list)) new-mat)))

(defn bitwise-not
  [mat]
  (let [new-mat (Mat.)
        _ (Core/bitwise_not mat new-mat)]
    new-mat))

(defn remove-background
  [mat]
  (let [new-mat (Mat.)
        threeshold-sHSV-channel (ssC/in-range (second (ssC/split-mat (ssI/toHSV mat))) 130 255)
        threeshold-bLab-channel (ssC/in-range (second (rest (ssC/split-mat (ssI/toLab mat)))) 135 255)
        mask-img (bitwise-or threeshold-bLab-channel threeshold-sHSV-channel)
        _ (.copyTo mat new-mat mask-img)]
    new-mat))
;
(defn further-filtering
  [mat]
  (let [new-mat (Mat.)
        green-magenta-channel (second (ssC/split-mat (ssI/toLab mat)))
        dark-gmc-threeshold   (ssC/in-range green-magenta-channel 125 255)
        light-gmc-threeshold  (ssC/in-range green-magenta-channel 135 255)
        blue-yellow-channel   (second (rest (ssC/split-mat (ssI/toLab mat))))
        byc-threeshold        (ssC/in-range blue-yellow-channel 200 255)
        new-mat (bitwise-or
                 dark-gmc-threeshold
                 light-gmc-threeshold
                 byc-threeshold)]
    new-mat))

;WRITE A MACRO a new def-withNewMat that set the function directly with the new variable new-mat,
;necessary to preserve immutability
