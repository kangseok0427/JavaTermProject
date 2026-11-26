package termproject;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class MissionFrame extends JFrame {
    private static final Color INK = new Color(26, 35, 48);
    private static final Color PAPER = new Color(241, 246, 249);
    private static final Color ACCENT = new Color(6, 103, 97);
    private static final String[] COLUMNS = {"번호", "미션 제목", "난이도", "통과 점수"};

    private final MissionRepository repository;
    private final MissionTable model = new MissionTable();
    private final JTable table = new JTable(model);
    private final JTextField keyword = new JTextField(16);
    private final JButton search = new JButton("조회");
    private final JButton reset = new JButton("새 미션");
    private final JButton save = new JButton("등록");
    private final JButton delete = new JButton("삭제");
    private final JTextField title = new JTextField();
    private final JTextField objective = new JTextField();
    private final JTextArea description = new JTextArea(5, 25);
    private final JComboBox<Integer> difficulty = new JComboBox<>(new Integer[]{1, 2, 3});
    private final JTextField passScore = new JTextField("60");
    private final JLabel status = new JLabel("Oracle에 연결하는 중입니다.");
    private final JLabel editorTitle = new JLabel("새 미션");
    private long selectedId;
    private boolean busy;

    public MissionFrame(MissionRepository repository) {
        super("AI 리터러시 미션 관리");
        this.repository = repository;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(860, 580));
        setSize(1040, 660);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(PAPER);
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));
        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel workspace = new JPanel(new BorderLayout(15, 0));
        workspace.setOpaque(false);
        workspace.add(buildList(), BorderLayout.CENTER);
        workspace.add(buildEditor(), BorderLayout.EAST);
        root.add(workspace, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);
        setContentPane(root);

        reset.addActionListener(event -> clearEditor());
        search.addActionListener(event -> load(selectedId, null));
        keyword.addActionListener(event -> {
            if (!busy) load(selectedId, null);
        });
        save.addActionListener(event -> saveMission());
        delete.addActionListener(event -> deleteMission());
        clearEditor();
        load(0, null);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(INK);
        header.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        JLabel heading = new JLabel("AI 리터러시 미션 관리");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 26));
        heading.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("학습 목표와 통과 기준을 관리하는 Oracle JDBC 도구");
        subtitle.setForeground(new Color(199, 216, 222));
        header.add(heading, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildList() {
        JPanel panel = new JPanel(new BorderLayout(0, 9));
        panel.setOpaque(false);
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        toolbar.setOpaque(false);
        JLabel filter = new JLabel("제목·목표 검색");
        filter.setLabelFor(keyword);
        toolbar.add(filter);
        toolbar.add(keyword);
        toolbar.add(search);
        panel.add(toolbar, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(29);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(240);
        table.getSelectionModel().addListSelectionListener(event -> {
            int row = table.getSelectedRow();
            if (!event.getValueIsAdjusting() && row >= 0) {
                showMission(model.rows.get(table.convertRowIndexToModel(row)));
            }
        });
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildEditor() {
        JPanel editor = new JPanel(new BorderLayout(0, 9));
        editor.setPreferredSize(new Dimension(390, 0));
        editor.setBackground(Color.WHITE);
        editor.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        editorTitle.setFont(editorTitle.getFont().deriveFont(Font.BOLD, 19));
        editorTitle.setForeground(ACCENT);
        editor.add(editorTitle, BorderLayout.NORTH);
        description.setWrapStyleWord(true);
        description.setLineWrap(true);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        addField(fields, 0, "미션 제목", title);
        addField(fields, 1, "학습 목표", objective);
        addField(fields, 2, "미션 설명", new JScrollPane(description));
        addField(fields, 3, "난이도", difficulty);
        addField(fields, 4, "통과 점수", passScore);
        editor.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);
        buttons.add(reset);
        buttons.add(save);
        buttons.add(delete);
        editor.add(buttons, BorderLayout.SOUTH);
        return editor;
    }

    private void addField(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints position = new GridBagConstraints();
        position.gridy = row;
        position.anchor = GridBagConstraints.NORTHWEST;
        position.insets = new Insets(4, 0, 4, 8);
        JLabel name = new JLabel(label);
        name.setLabelFor(field instanceof JScrollPane ? description : field);
        form.add(name, position);
        position.gridx = 1;
        position.weightx = 1;
        position.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, position);
    }

    private void clearEditor() {
        table.clearSelection();
        selectedId = 0;
        editorTitle.setText("새 미션");
        title.setText("");
        objective.setText("");
        description.setText("");
        difficulty.setSelectedItem(1);
        passScore.setText("60");
        updateActions();
    }

    private void showMission(Mission mission) {
        selectedId = mission.id();
        editorTitle.setText("미션 " + mission.id() + " 수정");
        title.setText(mission.title());
        objective.setText(mission.objective());
        description.setText(mission.description());
        description.setCaretPosition(0);
        difficulty.setSelectedItem(mission.difficulty());
        passScore.setText(mission.passScore().stripTrailingZeros().toPlainString());
        updateActions();
    }

    private void saveMission() {
        Mission mission;
        try {
            BigDecimal score = new BigDecimal(passScore.getText().strip());
            mission = new Mission(selectedId, title.getText(), objective.getText(), description.getText(),
                    (Integer) difficulty.getSelectedItem(), score);
        } catch (NumberFormatException error) {
            status.setText("통과 점수를 숫자로 입력하세요.");
            return;
        } catch (IllegalArgumentException error) {
            status.setText(error.getMessage());
            return;
        }

        if (mission.id() == 0) {
            runQuery(() -> repository.insert(mission), id -> {
                keyword.setText("");
                load(id, "미션을 등록했습니다.");
            });
        } else {
            runQuery(() -> repository.update(mission), changed -> {
                load(mission.id(), changed ? "미션을 수정했습니다." : "수정할 미션을 찾을 수 없습니다.");
            });
        }
    }

    private void deleteMission() {
        long id = selectedId;
        if (id == 0 || JOptionPane.showConfirmDialog(this, title.getText() + " 미션을 삭제할까요?",
                "미션 삭제 확인", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        runQuery(() -> repository.delete(id), changed -> {
            load(0, changed ? "미션을 삭제했습니다." : "삭제할 미션을 찾을 수 없습니다.");
        });
    }

    private void load(long selection, String message) {
        String searchText = keyword.getText().strip();
        runQuery(() -> repository.findAll(searchText), rows -> {
            clearEditor();
            model.replace(rows);
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i).id() == selection) {
                    int viewRow = table.convertRowIndexToView(i);
                    table.setRowSelectionInterval(viewRow, viewRow);
                    table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                    break;
                }
            }
            String count = "미션 " + rows.size() + "건";
            status.setText(message == null ? count + " 조회" : message + "  " + count);
        });
    }

    private <T> void runQuery(Callable<T> work, Consumer<T> success) {
        busy = true;
        updateActions();
        status.setText("Oracle에 요청하는 중입니다.");
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return work.call();
            }

            @Override
            protected void done() {
                busy = false;
                updateActions();
                try {
                    success.accept(get());
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    status.setText("요청이 중단됐습니다.");
                } catch (ExecutionException error) {
                    if (error.getCause() instanceof SQLException sqlError) {
                        status.setText(sqlError.getErrorCode() == 2292
                                ? "이 미션에 시도 기록이 연결되어 있어 삭제할 수 없습니다."
                                : "Oracle 요청 실패. 연결과 권한을 확인하세요. (오류 " + sqlError.getErrorCode() + ")");
                    } else {
                        status.setText("요청을 처리하지 못했습니다.");
                    }
                }
            }
        }.execute();
    }

    private void updateActions() {
        table.setEnabled(!busy);
        search.setEnabled(!busy);
        reset.setEnabled(!busy);
        save.setEnabled(!busy);
        save.setText(selectedId == 0 ? "등록" : "수정");
        delete.setEnabled(!busy && selectedId != 0);
    }

    private static final class MissionTable extends AbstractTableModel {
        private List<Mission> rows = List.of();

        void replace(List<Mission> rows) {
            this.rows = List.copyOf(rows);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int column) { return COLUMNS[column]; }

        @Override
        public Class<?> getColumnClass(int column) {
            return switch (column) {
                case 0 -> Long.class;
                case 2 -> Integer.class;
                case 3 -> BigDecimal.class;
                default -> String.class;
            };
        }

        @Override
        public Object getValueAt(int row, int column) {
            Mission mission = rows.get(row);
            return switch (column) {
                case 0 -> mission.id();
                case 1 -> mission.title();
                case 2 -> mission.difficulty();
                case 3 -> mission.passScore();
                default -> throw new IndexOutOfBoundsException(column);
            };
        }
    }
}
