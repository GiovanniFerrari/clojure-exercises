(import [org.opencv.core Mat Core Point Size MatOfDouble MatOfByte CvType]
           [org.opencv.imgproc Imgproc]
	   [org.opencv.highgui Highgui])


(def m1 (Mat.))  
(def m2 (Mat.)) 

(defn get-kernel-from-shape [size shape]
  (let [size2 (Size. (+ 1  (* size 2)) (+ 1 (* size 2)) )
        point (Point. size size)]
    (Imgproc/getStructuringElement shape size2 point)))

(def size 5)
(def lena(Highgui/imread "resources/images/lena.png"))

(Imgproc/dilate lena m1 (get-kernel-from-shape size, Imgproc/CV_SHAPE_ELLIPSE))

(Highgui/imwrite "resources/images/m1_first_dilate.png" m1)

(Imgproc/dilate m1 m2 (get-kernel-from-shape size, Imgproc/CV_SHAPE_ELLIPSE))

(Highgui/imwrite "resources/images/m2_second_dilate.png" m2)

(Imgproc/erode m2 m1 (get-kernel-from-shape size, Imgproc/CV_SHAPE_ELLIPSE))

(Highgui/imwrite "resources/images/m1_first_erode.png" m1)

(Imgproc/erode m1 m2 (get-kernel-from-shape size, Imgproc/CV_SHAPE_ELLIPSE))

(Highgui/imwrite "resources/images/m2_second_erode.png" m2)

(def lena_gray (Mat. (Size. (.rows lena) (.cols lena)) CvType/CV_8UC1 ))

(Imgproc/cvtColor lena lena_gray Imgproc/COLOR_BGR2GRAY);

(Highgui/imwrite "resources/images/lena_gray.png" lena_gray)

(Imgproc/Canny lena_gray m1 90 80)

(Highgui/imwrite "resources/images/lena_canny.png" m1)

(Imgproc/threshold lena_gray m1 40 255 Imgproc/THRESH_BINARY)

(Highgui/imwrite "resources/images/lena_thres.png" m1)

(defn repeated-close [in out size]
  (let [temp (Mat.) temp1 (Mat.)]
    (Imgproc/dilate in temp (get-kernel-from-shape size, Imgproc/CV_SHAPE_ELLIPSE))
    (Imgproc/dilate temp temp1 (get-kernel-from-shape size, Imgproc/CV_SHAPE_ELLIPSE))
    (Imgproc/erode temp1 temp (get-kernel-from-shape size, Imgproc/CV_SHAPE_ELLIPSE))
    (Imgproc/erode temp temp1 (get-kernel-from-shape size, Imgproc/CV_SHAPE_ELLIPSE))
    (Imgproc/medianBlur temp1 out size)
    ))

(defn leaves-mat-from-gray [gray]
  (let [edge (Mat.)
        mean (first (.val  (Core/mean gray)))
        mu (MatOfDouble.)
        sigma (MatOfDouble.)
        thresh 15]
    
    
    ;; edges (leaf texture) as indicators of leaves
    (Imgproc/Canny gray edge 18 (* thresh 2))

    (Core/meanStdDev gray mu sigma)
    (let [stdev (first (.get sigma 0 0))
          dark-thresh (max 3.0 (- mean (*  stdev (+ 0.2 
                                                    1.8)))) ; 0.0 ***
          light-thresh (min 253.0 (+ mean (* stdev 1.8)))
          thresh (Mat.) temp (Mat.)
          light (Mat.) lightopen (Mat.) dark (Mat.)   light-dark (Mat.)
          leaves (Mat.)  leaves-open (Mat.) leaves-closed (Mat.) diff (Mat.)]
      ;(debug (str "mean:" mean " stdev:" stdev " darkthr:" dark-thresh " lightthr:" light-thresh))

      ;; extract light areas
      (Imgproc/threshold gray thresh light-thresh 255 Imgproc/THRESH_BINARY)
      ;; large light areas
      (Imgproc/morphologyEx thresh lightopen Imgproc/MORPH_OPEN 
                            (get-kernel-from-shape 4 Imgproc/CV_SHAPE_ELLIPSE) )
      ;; small light areas (LED reflexes) that will be removed
      (Core/subtract thresh lightopen diff)
      (Imgproc/dilate diff light (get-kernel-from-shape 19 Imgproc/CV_SHAPE_CROSS))

      ;; dark area (holes) that will be removed
      (Imgproc/threshold gray thresh dark-thresh 255 Imgproc/THRESH_BINARY_INV)
      (Imgproc/dilate thresh dark (get-kernel-from-shape 5 Imgproc/CV_SHAPE_CROSS)) ; 9 ***
      
      (Core/add light dark light-dark)
      (Core/subtract edge light-dark leaves)

      (Imgproc/blur leaves temp (new Size 3 3))
      (Imgproc/threshold temp leaves-open 127 255 Imgproc/THRESH_BINARY)

      

      (repeated-close leaves-open leaves-closed 7)
      (if true
        (do  
          (Highgui/imwrite "resources/images/robo_00_gray.jpg" gray)
          (Highgui/imwrite "resources/images/robo_01_edge.jpg" edge)
          (Highgui/imwrite "resources/images/robo_02_dark.jpg"  dark)
          (Highgui/imwrite "resources/images/robo_03_diff.jpg"  diff)
          (Highgui/imwrite "resources/images/robo_04_light.jpg"  light)
          (Highgui/imwrite "resources/images/robo_05_leaves-open.jpg"  leaves-open)
	  (Highgui/imwrite "resources/images/robo_05_leaves-closed.jpg"  leaves-closed)))

      )
    
    )
  
  )


