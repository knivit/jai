package com.tsoft.jai.rag;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.base.Tuple;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.*;

import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.format;

@Data
@Accessors(chain = true)
public class Rag {

    private Config config;
    private String name;
    private String path;
    private Model embeddingModel;
    // ? hnsw: Hnsw<'static, f32, DistCosine>,
    // ? bm25: SearchEngine<DocumentId>,
    private RagData data;
    private String lastSources;

    // pub async fn init(
    //    config: &GlobalConfig,
    //    name: &str,
    //    save_path: &Path,
    //    doc_paths: &[String],
    //    abort_signal: AbortSignal,
    // ) -> Result<Self> {
    //    if !*IS_STDOUT_TERMINAL {
    //        bail!("Failed to init rag in non-interactive mode");
    //    }
    //    println!("⚙ Initializing RAG...");
    //    let (embedding_model, chunk_size, chunk_overlap) = Self::create_config(config)?;
    //    let (reranker_model, top_k) = {
    //        let config = config.read();
    //        (config.rag_reranker_model.clone(), config.rag_top_k)
    //    };
    //    let data = RagData::new(
    //        embedding_model.id(),
    //        chunk_size,
    //        chunk_overlap,
    //        reranker_model,
    //        top_k,
    //        embedding_model.max_batch_size(),
    //    );
    //    let mut rag = Self::create(config, name, save_path, data)?;
    //    let mut paths = doc_paths.to_vec();
    //    if paths.is_empty() {
    //        paths = add_documents()?;
    //    };
    //    let loaders = config.read().document_loaders.clone();
    //    let (spinner, spinner_rx) = Spinner::create("");
    //    abortable_run_with_spinner_rx(
    //        rag.sync_documents(&paths, true, loaders, Some(spinner)),
    //        spinner_rx,
    //        abort_signal,
    //    )
    //    .await?;
    //    if rag.save()? {
    //        println!("✓ Saved RAG to '{}'.", save_path.display());
    //    }
    //    Ok(rag)
    // }
    public static Rag init(Config config, String name, File savePath, List<String> docPaths, AbortSignal abortSignal) {
        return new Rag();
    }

    // pub fn load(config: &GlobalConfig, name: &str, path: &Path) -> Result<Self> {
    //    let err = || format!("Failed to load rag '{name}' at '{}'", path.display());
    //    let content = fs::read_to_string(path).with_context(err)?;
    //    let data: RagData = serde_yaml::from_str(&content).with_context(err)?;
    //    Self::create(config, name, path, data)
    // }
    public static Rag load(Config config, String name, File path) {
        RagData data = SerDe.readFromYamlFile(path, RagData.class);
        return create(config, name, path, data);
    }

    // pub fn create(config: &GlobalConfig, name: &str, path: &Path, data: RagData) -> Result<Self> {
    //    let hnsw = data.build_hnsw();
    //    let bm25 = data.build_bm25();
    //    let embedding_model =
    //        Model::retrieve_model(&config.read(), &data.embedding_model, ModelType::Embedding)?;
    //    let rag = Rag {
    //        config: config.clone(),
    //        name: name.to_string(),
    //        path: path.display().to_string(),
    //        data,
    //        embedding_model,
    //        hnsw,
    //        bm25,
    //        last_sources: RwLock::new(None),
    //    };
    //    Ok(rag)
    // }
    public static Rag create(Config config, String name, File path, RagData data) {
        return new Rag();
    }

    // pub fn export(&self) -> Result<String> {
    //    let files: Vec<_> = self
    //        .data
    //        .files
    //        .iter()
    //        .map(|(_, v)| {
    //            json!({
    //                "path": v.path,
    //                "num_chunks": v.documents.len(),
    //            })
    //        })
    //        .collect();
    //    let data = json!({
    //        "path": self.path,
    //        "embedding_model": self.embedding_model.id(),
    //        "chunk_size": self.data.chunk_size,
    //        "chunk_overlap": self.data.chunk_overlap,
    //        "reranker_model": self.data.reranker_model,
    //        "top_k": self.data.top_k,
    //        "batch_size": self.data.batch_size,
    //        "document_paths": self.data.document_paths,
    //        "files": files,
    //    });
    //    let output = serde_yaml::to_string(&data)
    //        .with_context(|| format!("Unable to show info about rag '{}'", self.name))?;
    //    Ok(output)
    // }
    public String export() {
        List<Value> files = data.getFiles().values().stream()
            .map(e -> new Value()
                .put("path", e.getPath())
                .put("num_chunks", isEmpty(e.getDocuments()) ? 0 : e.getDocuments().size()))
            .toList();
        Value value = new Value()
            .put("path", path)
            .put("embedding_model", embeddingModel.id())
            .put("chunk_size", data.getChunkSize())
            .put("chunk_overlap", data.getChunkOverlap())
            .put("reranker_model", data.getRerankerModel())
            .put("top_k", data.getTopK())
            .put("batch_size", data.getBatchSize())
            .put("document_paths", data.getDocumentPaths())
            .put("files", files);
        String output = SerDe.toYamlString(value);
        return output;
    }

    // pub async fn search(
    //    &self,
    //    text: &str,
    //    top_k: usize,
    //    rerank_model: Option<&str>,
    //    abort_signal: AbortSignal,
    // ) -> Result<(String, Vec<DocumentId>)> {
    //    let ret = abortable_run_with_spinner(
    //        self.hybird_search(text, top_k, rerank_model),
    //        "Searching",
    //        abort_signal,
    //    )
    //    .await;
    //    let (ids, documents): (Vec<_>, Vec<_>) = ret?.into_iter().unzip();
    //    let embeddings = documents.join("\n\n");
    //    Ok((embeddings, ids))
    // }
    public Result<Tuple<String, List<DocumentId>>> search(String text, Integer topK, String rerankModel, AbortSignal abortSignal) {
        return Ok(new Tuple<>(null, null));
    }

    // pub fn set_last_sources(&self, ids: &[DocumentId]) {
    //    let mut sources: IndexMap<String, Vec<String>> = IndexMap::new();
    //    for id in ids {
    //        let (file_index, _) = id.split();
    //        if let Some(file) = self.data.files.get(&file_index) {
    //            sources
    //                .entry(file.path.clone())
    //                .or_default()
    //                .push(format!("{id:?}"));
    //        }
    //    }
    //    let sources = if sources.is_empty() {
    //        None
    //    } else {
    //        Some(
    //            sources
    //                .into_iter()
    //                .map(|(path, ids)| format!("{path} ({})", ids.join(",")))
    //                .collect::<Vec<_>>()
    //                .join("\n"),
    //        )
    //    };
    //    *self.last_sources.write() = sources;
    // }
    public void setLastSources(List<DocumentId> ids) {
        Map<String, List<String>> sources = new LinkedHashMap<>();
        for (DocumentId id : ids) {
            Tuple<Integer, Integer> tuple = id.split();
            int fileIndex = tuple.first();
            RagFile file = data.getFiles().get(fileIndex);
            if (file != null) {
                sources
                    .computeIfAbsent(file.getPath(), (e) -> new ArrayList<>())
                    .add(format("{}", id.toStr()));
            }
        }
        String lastSources;
        if (isEmpty(sources)) {
            lastSources = null;
        } else {
            lastSources = String.join("\n", sources.entrySet().stream()
                .map(e -> format("{} ({})", e.getKey(), String.join(",", e.getValue())))
                .toList());
        }
        this.lastSources = lastSources;
    }

    // pub fn get_config(&self) -> (Option<String>, usize) {
    //    (self.data.reranker_model.clone(), self.data.top_k)
    // }
    public Tuple<String, Integer> getConfig() {
        return new Tuple<>(data.getRerankerModel(), data.getTopK());
    }

    // pub fn document_paths(&self) -> &[String] {
    //    &self.data.document_paths
    // }
    public List<String> documentPaths() {
        return (data == null) ? Collections.emptyList() : data.getDocumentPaths();
    }

    // pub async fn refresh_document_paths(
    //     &mut self,
    //     document_paths: &[String],
    //     refresh: bool,
    //     config: &GlobalConfig,
    //     abort_signal: AbortSignal,
    // ) -> Result<()> {
    //     let loaders = config.read().document_loaders.clone();
    //     let (spinner, spinner_rx) = Spinner::create("");
    //     abortable_run_with_spinner_rx(
    //         self.sync_documents(document_paths, refresh, loaders, Some(spinner)),
    //         spinner_rx,
    //         abort_signal,
    //     )
    //     .await?;
    //     if self.save()? {
    //         println!("✓ Saved rag to '{}'.", self.path);
    //     }
    //     Ok(())
    // }
    public void refreshDocumentPaths(List<String> documentPaths, boolean b, Config config, AbortSignal abortSignal) {

    }
}
