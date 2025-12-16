package com.ece.ertriage.ui;

import com.ece.ertriage.core.TriagePolicy;
import com.ece.ertriage.core.TriageQueueService;
import com.ece.ertriage.model.Patient;
import com.ece.ertriage.model.Severity;
import com.ece.ertriage.ui.benchmark.BenchmarkRunner;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class DashboardView extends BorderPane {

    private final TriageQueueService service = new TriageQueueService();

    private final TableView<Patient> table = new TableView<>();
    private final Label kpiWaiting = new Label("0");
    private final Label kpiTop = new Label("—");

    private final PieChart severityPie = new PieChart();

    private final CategoryAxis benchX = new CategoryAxis();
    private final NumberAxis benchY = new NumberAxis();
    private final BarChart<String, Number> benchChart = new BarChart<>(benchX, benchY);

    private final TextArea log = new TextArea();

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    public DashboardView() {
        setPadding(new Insets(12));
        setTop(buildHeader());
        setCenter(buildCenter());
        setRight(buildRightPanel());

        setupTable();
        setupCharts();
        setupTimer();
        seedDemoData();
        refreshUI();
    }

    private Pane buildHeader() {
        Label title = new Label("Acil Servis Triaj — Hasta Önceliklendirme");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700;");

        HBox kpis = new HBox(16,
                kpiCard("Bekleyen Hasta", kpiWaiting),
                kpiCard("En Öncelikli", kpiTop)
        );

        VBox left = new VBox(6, title, kpis);
        left.setPadding(new Insets(0, 0, 10, 0));
        return left;
    }

    private Pane kpiCard(String label, Label value) {
        Label l = new Label(label);
        l.setStyle("-fx-opacity: 0.75; -fx-font-size: 12px;");
        value.setStyle("-fx-font-size: 18px; -fx-font-weight: 800;");
        VBox box = new VBox(4, l, value);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #e5e7eb;");
        return box;
    }

    private Pane buildCenter() {
        VBox form = buildAddForm();

        VBox center = new VBox(10, form, table);
        center.setPadding(new Insets(0, 10, 0, 0));
        VBox.setVgrow(table, Priority.ALWAYS);
        return center;
    }

    private VBox buildAddForm() {
        TextField name = new TextField();
        name.setPromptText("Ad Soyad");

        Spinner<Integer> age = new Spinner<>(0, 120, 30);
        age.setEditable(true);

        ComboBox<Severity> severity = new ComboBox<>();
        severity.getItems().addAll(Severity.values());
        severity.setValue(Severity.MEDIUM);

        TextField complaint = new TextField();
        complaint.setPromptText("Şikayet (kısa)");

        CheckBox chronic = new CheckBox("Kronik");
        CheckBox pregnant = new CheckBox("Hamile");

        Button add = new Button("Hasta Ekle");
        add.setOnAction(e -> {
            String n = name.getText().trim();
            String c = complaint.getText().trim();
            if (n.isEmpty()) {
                toast("Ad Soyad zorunlu.");
                return;
            }
            if (c.isEmpty()) c = "—";

            Patient p = service.addPatient(n, age.getValue(), severity.getValue(), c, chronic.isSelected(), pregnant.isSelected());
            log("EKLE " + p.id() + " | " + p.name() + " | " + p.severity());
            name.clear();
            complaint.clear();
            chronic.setSelected(false);
            pregnant.setSelected(false);
            refreshUI();
        });

        Button next = new Button("Sıradaki Hastayı Çağır");
        next.setStyle("-fx-font-weight: 700;");
        next.setOnAction(e -> {
            service.rebuildHeapForNow();
            service.poll().ifPresentOrElse(p -> {
                int s = TriagePolicy.score(p, System.currentTimeMillis());
                log("ÇAĞIR " + p.id() + " (" + p.name() + ") skor=" + s);
                refreshUI();
            }, () -> toast("Bekleyen hasta yok."));
        });

        Button benchmark = new Button("Performans Testi");
        benchmark.setOnAction(e -> runBenchmark());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        int r = 0;
        grid.add(new Label("Ad Soyad"), 0, r); grid.add(name, 1, r++);
        grid.add(new Label("Yaş"), 0, r); grid.add(age, 1, r++);
        grid.add(new Label("Öncelik Seviyesi"), 0, r); grid.add(severity, 1, r++);
        grid.add(new Label("Şikayet"), 0, r); grid.add(complaint, 1, r++);

        HBox flags = new HBox(12, chronic, pregnant);
        grid.add(flags, 1, r++);

        HBox buttons = new HBox(10, add, next, benchmark);
        grid.add(buttons, 1, r++);

        VBox box = new VBox(8, new Label("Hasta Ekle"), grid);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #e5e7eb;");
        return box;
    }

    private Pane buildRightPanel() {
        Label chartsTitle = new Label("Grafikler & Benchmark");
        chartsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");

        severityPie.setTitle("Bekleyenlerde Öncelik Dağılımı");

        benchChart.setTitle("Performans: Heap (PriorityQueue) vs Liste + Sıralama");
        benchX.setLabel("Hasta sayısı (N)");
        benchY.setLabel("Süre (ms)");
        benchChart.setLegendVisible(true);
        benchChart.setCategoryGap(12);
        benchChart.setBarGap(4);

        log.setEditable(false);
        log.setPrefRowCount(8);

        VBox right = new VBox(10, chartsTitle, severityPie, benchChart, new Label("Olay Günlüğü"), log);
        right.setPrefWidth(440);
        right.setPadding(new Insets(0, 0, 0, 10));
        return right;
    }

    private void setupTable() {
        TableColumn<Patient, String> id = new TableColumn<>("ID");
        id.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id()));
        id.setPrefWidth(80);

        TableColumn<Patient, String> name = new TableColumn<>("Ad");
        name.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        name.setPrefWidth(140);

        TableColumn<Patient, String> sev = new TableColumn<>("Öncelik");
        sev.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().severity().name()));
        sev.setPrefWidth(100);

        TableColumn<Patient, String> score = new TableColumn<>("Skor (anlık)");
        score.setCellValueFactory(d -> {
            int s = TriagePolicy.score(d.getValue(), System.currentTimeMillis());
            return new SimpleStringProperty(String.valueOf(s));
        });
        score.setPrefWidth(110);

        TableColumn<Patient, String> wait = new TableColumn<>("Bekleme");
        wait.setCellValueFactory(d -> {
            long now = System.currentTimeMillis();
            long min = Math.max(0, (now - d.getValue().arrivalMillis()) / 60_000L);
            return new SimpleStringProperty(min + " dk");
        });
        wait.setPrefWidth(90);

        TableColumn<Patient, String> arrival = new TableColumn<>("Geliş");
        arrival.setCellValueFactory(d -> new SimpleStringProperty(TIME_FMT.format(Instant.ofEpochMilli(d.getValue().arrivalMillis()))));
        arrival.setPrefWidth(90);

        TableColumn<Patient, String> comp = new TableColumn<>("Şikayet");
        comp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().complaint()));
        comp.setPrefWidth(220);

        table.getColumns().setAll(id, name, sev, score, wait, arrival, comp);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void setupCharts() {
        severityPie.setLabelsVisible(true);
    }

    private void setupTimer() {
        Timeline t = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            service.rebuildHeapForNow();
            refreshUI();
        }));
        t.setCycleCount(Timeline.INDEFINITE);
        t.play();
    }

    private void refreshUI() {
        List<Patient> ordered = service.snapshotOrdered();
        table.getItems().setAll(ordered);

        kpiWaiting.setText(String.valueOf(service.waitingCount()));
        service.peek().ifPresentOrElse(p -> {
            int s = TriagePolicy.score(p, System.currentTimeMillis());
            kpiTop.setText(p.id() + " (" + p.severity() + ") skor=" + s);
        }, () -> kpiTop.setText("—"));

        updateSeverityPie(ordered);
    }

    private void updateSeverityPie(List<Patient> ordered) {
        Map<Severity, Integer> counts = new EnumMap<>(Severity.class);
        for (Severity s : Severity.values()) counts.put(s, 0);
        for (Patient p : ordered) counts.put(p.severity(), counts.get(p.severity()) + 1);

        severityPie.getData().clear();
        for (Severity s : Severity.values()) {
            int c = counts.get(s);
            if (c > 0) severityPie.getData().add(new PieChart.Data(s.name(), c));
        }
    }

    private void runBenchmark() {
        int[] sizes = {1000, 3000, 7000, 12000};

        BenchmarkRunner.Result r = BenchmarkRunner.run(sizes);

        benchChart.getData().clear();

        XYChart.Series<String, Number> heap = new XYChart.Series<>();
        heap.setName("Heap");

        XYChart.Series<String, Number> listSort = new XYChart.Series<>();
        listSort.setName("Liste+Sırala");

        for (int i = 0; i < sizes.length; i++) {
            heap.getData().add(new XYChart.Data<>(String.valueOf(sizes[i]), r.heapMs[i]));
            listSort.getData().add(new XYChart.Data<>(String.valueOf(sizes[i]), r.listSortMs[i]));
        }

        benchChart.getData().addAll(heap, listSort);
        log("TEST bitti. heap(ms)=" + join(r.heapMs) + " | liste+sirala(ms)=" + join(r.listSortMs));
    }

    private String join(long[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    private void seedDemoData() {
        service.addPatient("Ece", 21, Severity.MEDIUM, "Ateş", false, false);
        service.addPatient("Hilal", 22, Severity.HIGH, "Nefes darlığı", false, false);
        service.addPatient("Ahmet", 70, Severity.HIGH, "Göğüs ağrısı", true, false);
        service.addPatient("Elif", 31, Severity.CRITICAL, "Travma", false, true);
    }

    private void toast(String msg) {
        log("BİLGİ " + msg);
    }

    private void log(String msg) {
        log.appendText("[" + TIME_FMT.format(Instant.now()) + "] " + msg + "\n");
    }
}