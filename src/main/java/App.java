import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class App {
    private final Scanner scanner = new Scanner(System.in);
    private final List<WiseSaying> wiseSayings = new ArrayList<>();
    private int lastId = 0;

    public void run() {
        System.out.println("== 명언 앱 ==");

        while (true) {
            System.out.print("명령) ");
            Rq rq = new Rq(scanner.nextLine());

            switch (rq.getActionName()) {
                case "종료" -> {
                    return;
                }
                case "등록" -> actionWrite();
                case "목록" -> actionList();
                case "삭제" -> actionDelete(rq);
                case "수정" -> actionModify(rq);
                default -> System.out.println("알 수 없는 명령입니다.");
            }
        }
    }

    private void actionWrite() {
        System.out.print("명언 : ");
        String content = scanner.nextLine().trim();

        System.out.print("작가 : ");
        String author = scanner.nextLine().trim();

        int id = ++lastId;
        wiseSayings.add(new WiseSaying(id, content, author));

        System.out.println("%d번 명언이 등록되었습니다.".formatted(id));
    }

    private void actionList() {
        System.out.println("번호 / 작가 / 명언");
        System.out.println("----------------------");

        wiseSayings.reversed()
                .forEach(w -> System.out.println("%d / %s / %s".formatted(w.getId(), w.getAuthor(), w.getContent())));
    }

    private void actionDelete(Rq rq) {
        int id = rq.getParamAsInt("id", -1);

        if (id == -1) {
            System.out.println("id를 정확히 입력해주세요.");
            return;
        }

        boolean removed = wiseSayings.removeIf(w -> w.getId() == id);

        if (removed) System.out.println("%d번 명언이 삭제되었습니다.".formatted(id));
        else System.out.println("%d번 명언은 존재하지 않습니다.".formatted(id));
    }

    private void actionModify(Rq rq) {
        int id = rq.getParamAsInt("id", -1);

        if (id == -1) {
            System.out.println("id를 정확히 입력해주세요.");
            return;
        }

        Optional<WiseSaying> opWiseSaying = findById(id);

        if (opWiseSaying.isEmpty()) {
            System.out.println("%d번 명언은 존재하지 않습니다.".formatted(id));
            return;
        }

        WiseSaying wiseSaying = opWiseSaying.get();

        System.out.println("명언(기존) : %s".formatted(wiseSaying.getContent()));
        System.out.print("명언 : ");
        wiseSaying.setContent(scanner.nextLine().trim());

        System.out.println("작가(기존) : %s".formatted(wiseSaying.getAuthor()));
        System.out.print("작가 : ");
        wiseSaying.setAuthor(scanner.nextLine().trim());
    }

    private Optional<WiseSaying> findById(int id) {
        return wiseSayings.stream()
                .filter(w -> w.getId() == id)
                .findFirst();
    }
}
