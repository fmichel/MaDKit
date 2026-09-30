package madkit.samples;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.web.WebView;

/**
 * A small browser for the runnable examples contained in this module.
 *
 * <p>The catalog is deliberately explicit: README files describe the package and
 * only classes listed in that package's catalog can be launched. This avoids
 * accidentally executing helper or support classes discovered by reflection.
 */
public final class SamplesLauncher extends Application {

	private static final List<SamplePackage> PACKAGES = List.of(
			new SamplePackage("Launching", "/madkit/sample/launching/README.md", List.of(
					new Sample("Hello MaDKit", "madkit.sample.launching.HelloMaDKit"),
					new Sample("Multiple launching", "madkit.sample.launching.MultipleLaunching"),
					new Sample("Command-line options", "madkit.sample.launching.CommandLineOptions"),
					new Sample("Builder demo", "madkit.sample.launching.MadkitBuilderDemo"),
					new Sample("Builder options demo", "madkit.sample.launching.MadkitBuilderOptionsDemo"))),
			new SamplePackage("Agents", "/madkit/sample/agent/basic/README.md", List.of(
					new Sample("Simple agent", "madkit.sample.agent.basic.SimpleAgent"),
					new Sample("Agent parameters", "madkit.sample.agent.basic.AgentWithParameters"),
					new Sample("Threaded agent", "madkit.sample.agent.threaded.ThreadedAgent"),
					new Sample("Lifecycle launcher", "madkit.sample.agent.lifecycle.LifecycleLauncher"),
					new Sample("Daemon demo", "madkit.sample.agent.daemon.DaemonDemo"))),
			new SamplePackage("Organization", "/madkit/sample/organization/README.md", List.of(
					new Sample("Group and role demo", "madkit.sample.organization.GroupAndRoleDemo"),
					new Sample("Organization explorer", "madkit.sample.organization.OrganizationExplorer"),
					new Sample("Secured group demo", "madkit.sample.organization.secured.SecuredGroupDemo"))),
			new SamplePackage("Messaging", "/madkit/sample/messaging/README.md", List.of(
					new Sample("Messaging launcher", "madkit.sample.messaging.MessagingLauncher"),
					new Sample("Request/reply demo", "madkit.sample.messaging.RequestReplyDemo"),
					new Sample("Broadcast demo", "madkit.sample.messaging.BroadcastDemo"),
					new Sample("Enum dispatch demo", "madkit.sample.messaging.enumdispatch.EnumDispatchDemo"))),
			new SamplePackage("GUI", "/madkit/sample/gui/basic/README.md", List.of(
					new Sample("Default GUI agent", "madkit.sample.gui.basic.AgentWithDefaultGUI"),
					new Sample("Threaded GUI agent", "madkit.sample.gui.basic.ThreadedAgentWithGUI"),
					new Sample("Custom GUI agent", "madkit.sample.gui.custom.CustomGUIAgent"),
					new Sample("Property GUI agent", "madkit.sample.gui.properties.PropertyAgent"))),
			new SamplePackage("Logging and randomization", "/madkit/sample/logging/README.md", List.of(
					new Sample("Verbose agent", "madkit.sample.logging.VerboseAgent"),
					new Sample("Log file agent", "madkit.sample.logging.LogFileAgent"),
					new Sample("No-log agent", "madkit.sample.logging.NoLogAgent"),
					new Sample("Randomization demo", "madkit.sample.randomization.RandomizationDemo"))),
			new SamplePackage("ChartFX", "/madkit/sample/chartfx/README.md", List.of(
					new Sample("Population chart", "madkit.sample.chartfx.PopulationChartLauncher", "--start"),
					new Sample("Metric chart", "madkit.sample.chartfx.MetricChartLauncher", "--start"))));

	private final ExecutorService launcherExecutor = Executors.newCachedThreadPool(r -> {
		Thread thread = new Thread(r, "madkit-sample-launcher");
		thread.setDaemon(true);
		return thread;
	});
	private final WebView documentation = new WebView();
	private final VBox sampleButtons = new VBox(6);
	private final Parser markdownParser = Parser.builder().build();
	private final HtmlRenderer markdownRenderer = HtmlRenderer.builder().build();

	@Override
	public void start(Stage stage) {
		ListView<SamplePackage> packageList = new ListView<>(FXCollections.observableArrayList(PACKAGES));
		packageList.setCellFactory(view -> new ListCell<>() {
			@Override
			protected void updateItem(SamplePackage item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.name());
			}
		});
		packageList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) -> showPackage(selected));
		documentation.setContextMenuEnabled(false);

		BorderPane right = new BorderPane(documentation);
		Label heading = new Label("Runnable examples");
		heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		sampleButtons.setPadding(new Insets(8, 0, 0, 0));
		right.setTop(heading);
		right.setBottom(sampleButtons);
		BorderPane.setMargin(heading, new Insets(0, 0, 8, 0));
		BorderPane.setMargin(sampleButtons, new Insets(8, 0, 0, 0));

		SplitPane content = new SplitPane(packageList, right);
		content.setDividerPositions(0.25);
		Scene scene = new Scene(content, 1100, 700);
		stage.setTitle("MaDKit Samples");
		stage.setScene(scene);
		stage.setOnCloseRequest(event -> System.exit(0));
		stage.show();
		packageList.getSelectionModel().selectFirst();
	}

	private void showPackage(SamplePackage samplePackage) {
		if (samplePackage == null) {
			return;
		}
		documentation.getEngine().loadContent(renderMarkdown(readResource(samplePackage.readme())));
		sampleButtons.getChildren().clear();
		for (Sample sample : samplePackage.samples()) {
			Button button = new Button("Launch " + sample.name());
			button.setMaxWidth(Double.MAX_VALUE);
			button.setOnAction(event -> launch(sample));
			sampleButtons.getChildren().add(button);
		}
	}

	private void launch(Sample sample) {
		launcherExecutor.submit(() -> {
			try {
				Method main = Class.forName(sample.className()).getMethod("main", String[].class);
				main.invoke(null, (Object) sample.arguments());
			} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
				Throwable cause = exception instanceof InvocationTargetException invocation
						&& invocation.getCause() != null ? invocation.getCause() : exception;
				Platform.runLater(() -> showError(sample.name(), cause));
			}
		});
	}

	private void showError(String sampleName, Throwable cause) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Sample launch failed");
		alert.setHeaderText(sampleName);
		alert.setContentText(cause.toString());
		alert.show();
	}

	private static String readResource(String resource) {
		try (InputStream stream = SamplesLauncher.class.getResourceAsStream(resource)) {
			if (stream == null) {
				return "Documentation is not available: " + resource;
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			return "Unable to read documentation: " + exception.getMessage();
		}
	}

	private String renderMarkdown(String markdown) {
		String body = markdownRenderer.render(markdownParser.parse(markdown));
		return """
				<!DOCTYPE html>
				<html>
				<head>
				<meta charset="UTF-8">
				<style>
				body { font-family: sans-serif; color: #222; margin: 18px; line-height: 1.45; }
				h1, h2, h3 { color: #1f4e79; }
				code { font-family: monospace; background: #f1f3f5; padding: 2px 4px; }
				pre { background: #f1f3f5; padding: 12px; overflow-x: auto; }
				pre code { padding: 0; background: transparent; }
				table { border-collapse: collapse; margin: 10px 0; }
				th, td { border: 1px solid #b8c2cc; padding: 6px 10px; text-align: left; }
				th { background: #e9eef3; }
				blockquote { border-left: 4px solid #b8c2cc; margin-left: 0; padding-left: 12px; color: #555; }
				a { color: #1769aa; }
				</style>
				</head>
				<body>%s</body>
				</html>
				""".formatted(body);
	}

	@Override
	public void stop() {
		launcherExecutor.shutdownNow();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private record SamplePackage(String name, String readme, List<Sample> samples) {
	}

	private record Sample(String name, String className, String... arguments) {
	}
}