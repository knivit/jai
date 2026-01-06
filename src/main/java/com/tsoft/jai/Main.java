package com.tsoft.jai;

import com.tsoft.jai.cli.Cli;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.config.WorkingMode;

public class Main {

    // #[tokio::main]
    // async fn main() -> Result<()> {
    //    load_env_file()?;
    //    let cli = Cli::parse();
    //    let text = cli.text()?;
    //    let working_mode = if cli.serve.is_some() {
    //        WorkingMode::Serve
    //    } else if text.is_none() && cli.file.is_empty() {
    //        WorkingMode::Repl
    //    } else {
    //        WorkingMode::Cmd
    //    };
    //    let info_flag = cli.info
    //        || cli.sync_models
    //        || cli.list_models
    //        || cli.list_roles
    //        || cli.list_agents
    //        || cli.list_rags
    //        || cli.list_macros
    //        || cli.list_sessions;
    //    setup_logger(working_mode.is_serve())?;
    //    let config = Arc::new(RwLock::new(Config::init(working_mode, info_flag).await?));
    //    if let Err(err) = run(config, cli, text).await {
    //        render_error(err);
    //        std::process::exit(1);
    //    }
    //    Ok(())
    // }
    public static void main(String[] args) {
        Cli cli = Cli.parse(args);

        String text = cli.text();
        WorkingMode workingMode = WorkingMode.Cmd;
        if (cli.getServe() != null && !cli.getServe().isBlank()) {
            workingMode = WorkingMode.Serve;
        } else if ((text == null || text.isBlank()) && cli.getFile().isEmpty()) {
            workingMode = WorkingMode.Repl;
        }

        boolean infoFlag = cli.isInfo()
            || cli.isSyncModels()
            || cli.isListModels()
            || cli.isListRoles()
            || cli.isListAgents()
            || cli.isListRags()
            || cli.isListMacros()
            || cli.isListSessions();

        Config config = Config.init(workingMode, infoFlag, cli.getConfigFile());
    }
}
