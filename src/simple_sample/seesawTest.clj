(ns Test1 (:import [org.opencv.core
                    Mat Core Point Size MatOfDouble MatOfInt MatOfByte MatOfRect Scalar]
                   [org.opencv.imgcodecs Imgcodecs]
                   [org.opencv.imgproc Imgproc]
                   [java.awt.image BufferedImage]
		   [javax.swing ImageIcon]
		   ))

(use 'clojure.repl)
(use 'seesaw.core)

(native!) ;this will tell to seesaw to make thing native for the OS, for example thing for MAC OS


(def images_path "/home/giovanni/opencv_example_Sinapsi/src/")

(defn ReadImg [image_name]
  (Imgcodecs/imread (str images_path image_name)))

(def mat (ReadImg "BASIL.JPG"))

(println (.width mat))

(def bufferImageFromMat (atom (BufferedImage. (.width mat) (.height mat) 1)))


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

(def f (frame :title "Test SeeSaw Robonica"
              :size [300 :by 300]
              ))

(defn display [content]
  (config! f :content content) content)
;utilit
(def sldr (slider :orientation :vertical))

;(def lbl (label :icon (ImageIcon. "resources/images/Basil_gray.png")))
(def lbl (label :icon (ImageIcon. (to-buffered-image mat))))

(def split (left-right-split sldr lbl :divider-location 1/3))

;JAVA CODE
;Imgproc.blur(sourceImage, destImage, new Size(3.0, 3.0));

;(listen sldr :mouse-released (fn [e] (config! lbl :text (value e))))


(def mat2 (Mat.))
(listen sldr :mouse-released (fn [e] (do (Imgproc/blur mat mat2 (Size. (/ (value e) 5) (/ (value e) 5) ))
					(config! lbl :icon (ImageIcon. (to-buffered-image mat2))))))

(display split)
;(display lbl)

(-> f pack! show! )

