(ns Test1 (:import [org.opencv.core
                    Mat Core Point Size MatOfDouble MatOfInt MatOfByte MatOfRect Scalar]
                   [org.opencv.imgcodecs Imgcodecs]
                   [org.opencv.imgproc Imgproc]
                   [java.awt.image BufferedImage]
		               [javax.swing ImageIcon]
                   )
          (:require [simple-sample.utils :as ssu]
                    [simple-sample.Imgproc :as ssI]))

(use 'clojure.repl)
(use 'seesaw.core)

(native!) ;this will tell to seesaw to make thing native for the OS, for example thing for MAC OS


(def images_path "/home/giovanni/opencv_example_Sinapsi/src/")

(defn ReadImg [image_name]
  (Imgcodecs/imread (str images_path image_name)))

(def mat (ReadImg "tree.jpg"))

(def bufferImageFromMat (atom (BufferedImage. (.width mat) (.height mat) 1)))


(def f (frame :title "Test SeeSaw Robonica"
              :size [300 :by 300]
              ))

(defn display [content]
  (config! f :content content) content)
;utilit
(def sldr (slider :orientation :vertical))

;(def lbl (label :icon (ImageIcon. "resources/images/Basil_gray.png")))
(def lbl (label :icon (ImageIcon. (ssu/to-buffered-image mat))))

(def split (left-right-split sldr lbl :divider-location 1/3))

;JAVA CODE
;Imgproc.blur(sourceImage, destImage, new Size(3.0, 3.0));

;(listen sldr :mouse-released (fn [e] (config! lbl :text (value e))))


(def mat2 (Mat.))
(listen sldr :mouse-released (fn [e] (do (Imgproc/blur mat mat2 (Size. (/ (value e) 5) (/ (value e) 5) ))
					(config! lbl :icon (ImageIcon. (ssu/to-buffered-image mat2))))))

(display split)
;(display lbl)

;(-> f pack! show! )
