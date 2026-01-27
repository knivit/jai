package com.tsoft.jai.client.macros;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.Client;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.client.model.ModelType;
import com.tsoft.jai.config.ClientConfig;
import com.tsoft.jai.config.Config;

import java.util.Collections;
import java.util.List;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;

public class Macros {

    // pub fn list_models(local_config: &$config) -> Vec<Model> {
    //    let client_name = Self::name(local_config);
    //    if local_config.models.is_empty() {
    //        if let Some(v) = $crate::client::ALL_PROVIDER_MODELS.iter().find(|v| {
    //            v.provider == $name ||
    //                ($name == OpenAICompatibleClient::NAME
    //                    && local_config.name.as_ref().map(|name| name.starts_with(&v.provider)).unwrap_or_default())
    //        }) {
    //            return Model::from_config(client_name, &v.models);
    //        }
    //        vec![]
    //    } else {
    //        Model::from_config(client_name, &local_config.models)
    //    }
    // }
    public static List<Model> listModels(ClientConfig clientConfig) {
        String clientName = clientConfig.getName();;
        return Model.fromConfig(clientName, clientConfig.getModels());
    }

    // pub fn list_client_names(config: &$crate::config::Config) -> Vec<&'static String> {
    //    let names = ALL_CLIENT_NAMES.get_or_init(|| {
    //        config
    //            .clients
    //            .iter()
    //            .flat_map(|v| match v {
    //                $(ClientConfig::$config(c) => vec![$client::name(c).to_string()],)+
    //                ClientConfig::Unknown => vec![],
    //            })
    //            .collect()
    //    });
    //    names.iter().collect()
    // }
    public static List<String> listClientNames(Config config) {
        List<ClientConfig> clients = config.getClients();
        if (clients == null || clients.isEmpty()) {
            return Collections.emptyList();
        }
        return clients.stream()
            .map(ClientConfig::getName)
            .toList();
    }

    // pub fn list_all_models(config: &$crate::config::Config) -> Vec<&'static $crate::client::Model> {
    //    let models = ALL_MODELS.get_or_init(|| {
    //        config
    //            .clients
    //            .iter()
    //            .flat_map(|v| match v {
    //                $(ClientConfig::$config(c) => $client::list_models(c),)+
    //                ClientConfig::Unknown => vec![],
    //            })
    //            .collect()
    //    });
    //    models.iter().collect()
    // }
    public static List<Model> listAllModels(Config config) {
        List<ClientConfig> clients = config.getClients();
        if (clients == null || clients.isEmpty()) {
            return Collections.emptyList();
        }
        return clients.stream()
            .flatMap(e -> listModels(e).stream())
            .toList();
    }

    // pub fn list_models(config: &$crate::config::Config, model_type: $crate::client::ModelType) -> Vec<&'static $crate::client::Model> {
    //    list_all_models(config).into_iter().filter(|v| v.model_type() == model_type).collect()
    // }
    public static List<Model> listModels(Config config, ModelType modelType) {
        return listAllModels(config).stream()
            .filter(e -> modelType.equals(e.getModelType()))
            .toList();
    }

    // pub fn init_client(config: &$crate::config::GlobalConfig, model: Option<$crate::client::Model>) -> anyhow::Result<Box<dyn Client>> {
    //    let model = model.unwrap_or_else(|| config.read().model.clone());
    //    None
    //    $(.or_else(|| $client::init(config, &model)))+
    //    .ok_or_else(|| {
    //        anyhow::anyhow!("Invalid model '{}'", model.id())
    //    })
    // }
    public static Result<Client> initClient(Config config, Model model) {
        if (model == null) {
            model = config.getModel();
        }
        Client client = Client.init(config, model);
        if (client == null) {
            return Err("Invalid model '{}'", model.id());
        }
        return Ok(client);
    }
}
