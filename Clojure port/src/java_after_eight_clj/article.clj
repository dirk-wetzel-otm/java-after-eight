(ns java-after-eight-clj.article
  (:require [java-after-eight-clj.util :as util]
            [clojure.string :as string])
  (:import [java.time LocalDate]
           [java.nio.file Files]
           [java.io File]))

;; Note: I will try to stay away from introducing new types, as much as possible,
;;       but instead use basic Clojure data structures as long as they suffice.


;; below here -> .article.Title

(defn ^:private create-title
  [text]
  (-> (util/remove-outer-quotation-marks text)
      util/strip
      (util/assert-not-empty "Titles can't have an empty text.")))


;; below here -> .article.Description

(defn ^:private create-description
  [text]
  (-> (util/remove-outer-quotation-marks text)
      (util/assert-not-empty "Description can't have an empty text.")))

;; below here -> .article.Slug

(defn ^:private create-slug
  [value]
  (util/assert-not-empty value "Slugs can't have an empty value."))


;; below here -> .article.Tag

(defn ^:private create-tags
  "Creates a set of tags from the given text"
  [tags-text]
  (let [raw-tags (-> tags-text
                     (string/replace #"^\[|\]$" "")
                     (string/split #","))]
    (->> raw-tags
         (map util/strip)
         (filter seq)
         set)))


;; below here -> .article.ArticleFactory

(defn ^:private not-front-matter-separator?
  [line]
  (not= "---" (util/strip line)))


(defn ^:private skip-next-front-matter-separator
  [lines]
  (->> lines
       (drop-while not-front-matter-separator?)
       (drop 1)))


(defn ^:private extract-front-matter
  [lines]
  (->> lines
       skip-next-front-matter-separator
       (take-while not-front-matter-separator?)))


(defn ^:private extract-content
  [lines]
  (->> lines
       skip-next-front-matter-separator
       skip-next-front-matter-separator))


(defn ^:private line->key-value-pair
  [line]
  (let [[k v] (string/split line #":" 2)]
    (when (nil? v)
      (throw (ex-info "Line doesn't seem to be a key/value pair (no colon)." {:line line})))
    (when (string/blank? k)
      (throw (ex-info "Line has no key." {:line line})))
    [(-> k util/strip string/lower-case keyword)
     (util/strip v)]))


;; equivalents of ArticleFactory.createArticle(..)

(defrecord Article [title tags date description slug content-fn])

(defn ^:private parse-article
  ^Article [front-matter content-fn]
  (let [{:keys [title tags date description slug]}
        (->> front-matter
             (map line->key-value-pair)
             (into {}))]
    (->Article
      (create-title title)
      (create-tags tags)
      (LocalDate/parse date)
      (create-description description)
      (create-slug slug)
      content-fn)))


(defn parse-article-from-lines
  [lines]
  (parse-article
    (extract-front-matter lines)
    #(extract-content lines)))


(defn parse-article-from-file
  [^File file]
  (try
    (let [lines-fn #(-> file .toPath Files/readAllLines)]
      (parse-article
        (extract-front-matter (lines-fn))
        (comp extract-content lines-fn)))
    (catch Exception ex
      (throw (ex-info "Creating article failed" {:file file} ex)))))
