(ns Test1 (:import [org.opencv.core
          Mat Core Point Size MatOfDouble MatOfInt MatOfByte MatOfRect Scalar]
         [org.opencv.imgcodecs Imgcodecs]
         [org.opencv.imgproc Imgproc]
         ))

(def images_path "/home/giovanni/another_stagedir/simple-sample/resources/images/")

(defn ReadImg [image_name]
  (Imgcodecs/imread (str images_path image_name)))

(defn WriteImg [ InputImg image_name]
  (Imgcodecs/imwrite (str images_path image_name) InputImg))

(defn ToGray [InputMat]
  (let [NewMat (Mat.)]
    (Imgproc/cvtColor InputMat NewMat Imgproc/COLOR_BGR2GRAY )
    NewMat
    ))

;(Imgcodecs/imwrite (str images_path "gray_Coin.jpg") (ToGray (ReadImg "Coins.jpg"))) ;; Names of the file

(defn CannyTo [InputImg threshold_low threshold_high]
  (let [tmp (Mat.)]
    (Imgproc/Canny InputImg tmp threshold_low threshold_high)
    tmp
    ))


(defn PreprocessImage [InputImg]
  (let [tmp (Mat.)]
    (Imgproc/GaussianBlur InputImg tmp (Size. 7 5) 5.)
    tmp
    ))

(defn GetCircles [InputImg]
  (let [Circles (Mat.)]
    (Imgproc/HoughCircles InputImg Circles Imgproc/CV_HOUGH_GRADIENT 1 10)
    Circles
    ))

;(def Matrix_of_circles (GetCircles (ToGray (ReadImg "Coins.jpg"))))

(defn drawCircle [InputImg]
  (let [temp (GetCircles (ToGray (PreprocessImage InputImg)))]
        (loop [number_of_circles (dec (.cols temp))]
          (if (< number_of_circles 0)
              InputImg
              (let [x (nth (vec (.get temp 0 number_of_circles)) 0)
                    y (nth (vec (.get temp 0 number_of_circles)) 1)
                    r (nth (vec (.get temp 0 number_of_circles)) 2)]
                (Imgproc/circle InputImg
                        (Point. x y)
                        r
                        (Scalar. 0 0 255)
                        5)
                (recur (dec number_of_circles)))))));

(defn drawCircleOnImage [FileName]
  (Imgcodecs/imwrite
    (str images_path "WithCircle" FileName)
    (drawCircle (ReadImg FileName)))) ;; Names of the file
