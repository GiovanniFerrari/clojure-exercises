(ns simple-sample.Core (:import [org.opencv.core
                                    Mat Core Point Size MatOfDouble MatOfInt MatOfByte MatOfRect Scalar]
                                   [org.opencv.imgcodecs Imgcodecs]
                                   [org.opencv.imgproc Imgproc]
                                   [java.awt.image BufferedImage]
                                   [javax.swing ImageIcon])
                        )

;
(use 'seesaw.core)

(native!) ;this will tell to seesaw to make thing native for the OS, for example thing for MAC OS

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
