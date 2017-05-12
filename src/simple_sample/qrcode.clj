(ns simple-sample.qrcode (:import [org.opencv.core
                                   Mat Core Point Size MatOfDouble MatOfInt MatOfByte MatOfRect Scalar]
                                  [org.opencv.imgcodecs Imgcodecs]
                                  [org.opencv.imgproc Imgproc]
                                  [java.awt.image BufferedImage]
                                  [javax.swing ImageIcon])
    (:require [simple-sample.utils :as ssu]))

(def find-qrcode
  [mat])
