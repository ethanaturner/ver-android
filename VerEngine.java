//
//  VerEngine.swift
//  Ver
//
//  Created by Ethan Turner on 12/26/16.
//  Copyright © 2016 Ethan Turner. All rights reserved.
//

class VerEngine {
    typealias CompletionHandler = (_ success:Bool) -> Void
    typealias DataCompletionHandler = (_ success: Bool, _ data: [String: Any]) -> Void
    typealias MultipleDataCompletionHandler = (_ success: Bool, _ data: NSDictionary) -> Void

    private void startEngine(title: String, type: String, completion: @escaping CompletionHandler) {
        let encodedTitle = title.replacingOccurrences(of: " ", with: "+");
        let url: String = Constants.guideBoxApiURL + search?type= type &query=\(encodedTitle)&" + Constants.guideBoxApiKey;
    }

    public void startEngineForCollection(title: String, type: String, completion: @escaping CompletionHandler) {
        let encodedTitle = title.replacingOccurrences(of: " ", with: "+");
        let url: String = Constants.guideBoxApiURL + "search?type=\(type)&query=\(encodedTitle)&" + Constants.guideBoxApiKey;
    }

    public void singleSearch(id: String, type: String, completion: @escaping DataCompletionHandler) {
        var typeFinal: String? = ""
        if (type == "show") {
            typeFinal = "shows";
        } else if (type == "movie") {
            typeFinal = "movies";
        }
        let url: String = Constants.guideBoxApiURL + typeFinal + "/" + id + Constants.guideBoxApiKey;
    }

    public void singleSearchForCollection(id: String, type: String, key: String, completion: @escaping DataCompletionHandler) {
        var typeFinal: String? = ""
        if (type == "show") {
            typeFinal = "shows";
        } else if (type == "movie") {
            typeFinal = "movies";
        }
        let url: String = Constants.guideBoxApiURL + typeFinal + "/" + id + Constants.guideBoxApiKey;
    }

    public void getShowSources(id: String, completion: @escaping MultipleDataCompletionHandler) {
        let url: String = Constants.guideBoxApiURL + "shows/" + id + "/available_content?" + Constants.guideBoxApiKey;
    }

}
