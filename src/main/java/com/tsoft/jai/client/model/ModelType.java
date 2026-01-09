package com.tsoft.jai.client.model;

public enum ModelType {

    Chat,
    Embedding,
    Reranker;

    // pub fn can_create_from_name(self) -> bool {
    //    match self {
    //        ModelType::Chat => true,
    //        ModelType::Embedding => false,
    //        ModelType::Reranker => true,
    //    }
    // }
    public static boolean canCreateFromName(ModelType modelType) {
        return ModelType.Chat.equals(modelType) || ModelType.Reranker.equals(modelType);
    }
}
