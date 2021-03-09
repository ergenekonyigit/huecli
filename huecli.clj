(require '[babashka.curl :as curl]
         '[cheshire.core :as json])

(def bridge-ip (System/getenv "HUE_BRIDGE_IP"))
(def bridge-username (System/getenv "HUE_USERNAME"))
(def bridge-api (str "http://" bridge-ip "/api/" bridge-username "/"))

(defn hue-get-light-by-id [id]
  (-> (curl/get (str bridge-api "lights/" id))
      :body
      (json/parse-string true)))

(defn hue-update-light-by-id [id state]
  (let [status (-> (curl/put (str bridge-api "lights/" id "/state")
                             {:body (json/generate-string {:on state})})
                   :status)]
    (if (= status 200)
      (str "light " id " is " (if state "opened" "closed"))
      "something went wrong")))

(let [[id state] *command-line-args*]
  (when (or (empty? id) (empty? state))
    (println "Usage: <id> <state>")
    (System/exit 1))
  (hue-update-light-by-id id (Boolean/valueOf state)))
