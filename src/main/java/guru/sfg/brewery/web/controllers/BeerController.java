package guru.sfg.brewery.web.controllers;


import guru.sfg.brewery.domain.Beer;
import guru.sfg.brewery.repositories.BeerInventoryRepository;
import guru.sfg.brewery.repositories.BeerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;


@RequestMapping("/beers")
@Controller
public class BeerController {

    //ToDO: Add service
    private BeerRepository beerRepository;
    private BeerInventoryRepository beerInventoryRepository;

    public BeerController(BeerRepository beerRepository, BeerInventoryRepository beerInventoryRepository) {
        this.beerRepository = beerRepository;
        this.beerInventoryRepository = beerInventoryRepository;
    }

    /*
     * показывает HTML-страницу с формой поиска пива, предварительно положив в модель пустой
     * объект Beer, чтобы форма могла привязаться к нему для ввода названия.
     * Spring сам создает объект Model и передает его в метод, когда видит,
     * что ты указал его в параметрах, а внутри метода ты просто наполняешь
     * этот объект данными, которые потом Spring отдаст Thymeleaf для отрисовки HTML.
     */
    @RequestMapping("/find")
    public String findBeers(Model model) {
        model.addAttribute("beer", Beer.builder().build());
        return "beers/findBeers";
    }

    /*
     * Ищет пиво по введенному имени: если ничего не нашел — возвращает
     * форму с ошибкой, если нашел одно — перекидывает на его страницу,
     * если несколько — показывает список
     */
    @GetMapping
    public String processFindFormReturnMany(Beer beer, BindingResult result, Model model) {
        /*
         * ищет пиво по введенному имени: если ничего не нашел — возвращает форму с ошибкой,
         * если нашел одно — перекидывает на его страницу, если несколько — показывает список
         */
        // find beers by name
        //ToDO: Add Service
        //ToDO: Get paging data from view
        /*
         * ищет в базе данных пиво по имени (beer.getBeerName()) и возвращает
         * первую страницу из 10 записей, отсортированных по имени в обратном порядке
         */
        Page<Beer> pagedResult = beerRepository.findAllByBeerName(beer.getBeerName(), createPageRequest(0, 10, Sort.Direction.DESC, "beerName"));
        List<Beer> beerList = pagedResult.getContent();
        if (beerList.isEmpty()) {
            // no beers found
            result.rejectValue("beerName", "notFound", "not found"); // добавляет в BindingResult ошибку для поля beerName с кодом "notFound" и текстом "not found", чтобы затем отобразить её на форме под полем ввода
            return "beers/findBeers";
        } else if (beerList.size() == 1) {
            // 1 beer found
            beer = beerList.get(0);
            return "redirect:/beers/" + beer.getId(); // перенаправляет пользователя на страницу с деталями найденного пива по его ID (браузер сделает новый GET-запрос на этот адрес)
        } else {
            // multiple beers found
            model.addAttribute("selections", beerList);
            return "beers/beerList";
        }
    }


    @GetMapping("/{beerId}")
    public ModelAndView showBeer(@PathVariable UUID beerId) {
        ModelAndView mav = new ModelAndView("beers/beerDetails");
        //ToDO: Add Service
        mav.addObject(beerRepository.findById(beerId).get()); // findById() возвращает не сам объект, а контейнер Optional<Beer> (который может быть пустым, если пива с таким ID нет), а .get() уже достает из него сам объект Beer
        return mav;
    }

    @GetMapping("/new")
    public String initCreationForm(Model model) { // открывает пустую HTML-форму для ввода
        model.addAttribute("beer", Beer.builder().build()); // создает пустой объект Beer и кладет его в модель (чтобы Thymeleaf-форма на странице createBeer могла привязаться к нему для ввода данных), после чего возвращает имя шаблона beers/createBeer для отображения
        return "beers/createBeer";
    }

    /*
     * Когда пользователь нажимает кнопку «Submit» (type="submit"), браузер
     * отправляет POST-запрос на /beers/new с данными из формы, и Spring автоматически
     * находит метод processCreationForm, создает из этих данных объект Beer и передает его в метод.
     * Spring создает объект Beer благодаря типу параметра в методе, а th:object="${beer}
     * нужен Thymeleaf для привязки полей формы к этому объекту.
     */
    @PostMapping("/new")
    public String processCreationForm(Beer beer) { // принимает данные из формы ↑ и сохраняет новое пиво в базу данных
        //ToDO: Add Service
        Beer newBeer = Beer.builder()
                .beerName(beer.getBeerName())
                .beerStyle(beer.getBeerStyle())
                .minOnHand(beer.getMinOnHand())
                .price(beer.getPrice())
                .quantityToBrew(beer.getQuantityToBrew())
                .upc(beer.getUpc())
                .build();

        Beer savedBeer = beerRepository.save(newBeer);
        return "redirect:/beers/" + savedBeer.getId(); // редирект на страницу с деталями только что созданного пива по адресу /beers/{id}
    }

    @GetMapping("/{beerId}/edit") // находит пиво по ID, кладет его в модель и открывает страницу с формой для его редактирования
    public String initUpdateBeerForm(@PathVariable UUID beerId, Model model) {
        if (beerRepository.findById(beerId).isPresent())
            model.addAttribute("beer", beerRepository.findById(beerId).get());
        return "beers/createOrUpdateBeer";
    }

    @PostMapping("/{beerId}/edit")
    public String processUpdationForm(@Valid Beer beer, BindingResult result) {
        if (result.hasErrors()) {
            return "beers/createOrUpdateBeer";
        } else {
            //ToDO: Add Service (вынести эту логику в отдельный сервис)
            Beer savedBeer = beerRepository.save(beer);
            return "redirect:/beers/" + savedBeer.getId();
        }
    }

    /*
     * создает объект PageRequest (запрос на конкретную страницу данных),
     * передавая в него номер страницы, ее размер и правило сортировки по указанному полю
     */
    private PageRequest createPageRequest(int page, int size, Sort.Direction sortDirection, String propertyName) {
        return PageRequest.of(page,
                size,
                Sort.by(sortDirection, propertyName)
        );
    }
}


