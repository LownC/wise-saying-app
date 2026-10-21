import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class App {
    private Scanner scanner = new Scanner(System.in);
    private List<WiseSaying> wiseSayings = new ArrayList<>();
    private int lastId;

    public void run(){
        System.out.println("== 명언 앱 ==");

        while(true){
            System.out.print("명령) ");
            String cmd = scanner.nextLine().trim();

            if(cmd.equals("종료")){
                break;
            }

            else if(cmd.equals("등록")){
                addWiseSaying();
            }

            else if(cmd.equals("목록")){
                showWiseSayingList();
            }

            else if(cmd.startsWith("삭제?id=")){
                int deleteId = Integer.parseInt(cmd.split("=")[1]);
                deleteWiseSayingById(deleteId);
            }

            else if(cmd.startsWith("수정?id=")){
                int modifyId = Integer.parseInt(cmd.split("=")[1]);
                modifyWiseSayingById(modifyId);
            }
        }
        scanner.close();
    }

    private void modifyWiseSayingById(int modifyId) {

        Optional<WiseSaying> found = wiseSayings.stream()
                .filter(w -> w.getId() == modifyId)
                .findFirst();
        if(!found.isPresent())
            System.out.println("%d번 명언은 존재하지 않습니다.".formatted(modifyId));

        else
            modifyContentAndAuthor(found.get());
    }

    private void modifyContentAndAuthor(WiseSaying modifyWiseSaying) {

        System.out.println("명언(기존) %s".formatted(modifyWiseSaying.getContent()));
        System.out.print("명언 : ");
        modifyWiseSaying.setContent(scanner.nextLine().trim());

        System.out.println("작가(기존) : %s".formatted(modifyWiseSaying.getAuthor()));
        System.out.print("작가 : ");
        modifyWiseSaying.setAuthor(scanner.nextLine().trim());
    }

    private boolean isWiseSayingExistById(int checkId) {
        return wiseSayings
                .stream()
                .anyMatch(w->w.getId() == checkId);
    }

    private void deleteWiseSayingById(int deleteId) {
        Optional<WiseSaying> found = wiseSayings.stream()
                .filter(w -> w.getId() == deleteId)
                .findFirst();
        if(!found.isPresent())
            System.out.println("%d번 명언은 존재하지 않습니다.".formatted(deleteId));

        else{
            wiseSayings.remove(found.get());
            System.out.println("%d번 명언이 삭제되었습니다.".formatted(deleteId));
        }
    }

    private void showWiseSayingList(){
        List<WiseSaying> sortedWiseSayings = wiseSayings.reversed();

        System.out.println("번호 / 작가 / 명언");
        System.out.println("-------------------");
        for(WiseSaying w : sortedWiseSayings)
            System.out.println("%d / %s / %s".formatted(w.getId(), w.getAuthor(), w.getContent()));
    }

    private void addWiseSaying(){
        System.out.print("명언 : ");
        String content = scanner.nextLine().trim();

        System.out.print("작가 : ");
        String author = scanner.nextLine().trim();

        int id = ++lastId;

        wiseSayings.add(new WiseSaying(id,content,author));

        System.out.println("%d번 명언이 등록되었습니다.".formatted(id));
    }
}
