(ns simple-sample.utils (:import [org.opencv.core
                                  Mat Core Point Size MatOfDouble MatOfInt MatOfByte MatOfRect Scalar]
                                 [org.opencv.imgcodecs Imgcodecs]
                                 [org.opencv.imgproc Imgproc]
                                 [java.awt.image BufferedImage]
                                 [javax.swing ImageIcon]))

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

(defn apply-upper-threshold
  "A function that takes as argumenst a matrix object and an integer n.
  It returns a different matrix object with apply a threshold from n to 255 "
  [mat n]
  (let [new_mat (Mat.)
        _ (Imgproc/threshold  mat new_mat n 255 Imgproc/THRESH_BINARY)]
    new_mat))

;
(defn apply-upper-threshold!
  "A function that takes as argumenst a matrix object and an integer n.
  It returns the same matrix object with apply a threshold from n to 255 "
  [mat n]
  (let [_ (Imgproc/threshold  mat mat n 255 Imgproc/THRESH_BINARY)]
    mat))

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
(defn toHSV
  "take a matrix object and return a new one in Hue Saturation Value color model."
  [mat]
  (let [new-mat (Mat.)
        _ (Imgproc/cvtColor mat new-mat Imgproc/COLOR_BGR2HSV)]
    new-mat))
;

(defn toLab
  "take a matrix object and return a new one in Hue Saturation Value color model."
  [mat]
  (let [new-mat (Mat.)
        _ (Imgproc/cvtColor mat new-mat Imgproc/COLOR_BGR2Lab)]
    new-mat))

;(Core/inRange mat (Scalar. 0 255 100) (Scalar. 255 255 255) new-mat)
(defn test-HSV
  "only a temporary function to make some test about HSV property of an image
  IT HAS A BIG PROBLEM WHEN LOWER LIMIT IS SETTLED TO 0"
  [mat]
  (let [old-mat (toHSV mat)
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
(defn split-mat
  "take a mat with three channel and return a java.util.ArrayList object
  containing three mat, one for each channel of the original matrix"
  [mat]
  (let [mat-array (java.util.ArrayList.)
        _ (Core/split mat mat-array)]
    mat-array))

(defn in-range
  [mat low-limit up-limit]
  (let [new-mat (Mat.)
        _ (Core/inRange mat (Scalar. low-limit 0 0) (Scalar. up-limit 0 0) new-mat)]
    new-mat))

;
(defn gaussian-blur
  [mat square-side sigma]
  (let [new-mat (Mat.)
        _ (Imgproc/GaussianBlur mat new-mat (Size. square-side square-side) sigma)]
    new-mat))

(defn median-blur
  [mat sigma]
  (let [new-mat (Mat.)
        _ (Imgproc/medianBlur mat new-mat sigma)]
    new-mat))

(defn bitwise-or
  [mat1 mat2 & mat-list]
  (let [new-mat (Mat.)
        _ (Core/bitwise_or mat1 mat2 new-mat)]
    (if-not (empty? mat-list) (apply bitwise-or new-mat (first mat-list) (rest mat-list)) new-mat)))


(defn remove-background
  [mat]
  (let [new-mat (Mat.)
        threeshold-sHSV-channel (in-range (second (split-mat (toHSV mat))) 130 255)
        threeshold-bLab-channel (in-range (second (rest (split-mat (toLab mat)))) 135 255)
        mask-img (bitwise-or threeshold-bLab-channel threeshold-sHSV-channel)
        _ (.copyTo mat new-mat mask-img)]
        new-mat))


;WRITE A MACRO a new def-withNewMat that set the function directly with the new variable new-mat,
;necessary to preserve immutability
