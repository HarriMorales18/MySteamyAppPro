import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.scss'],
})
export class CardComponent {
  @Input() game: any;
  @Input() isFavorite: boolean = false;
  @Output() onFavorite = new EventEmitter<any>();

  handleFavorite(event: Event) {
    event.stopPropagation();
    this.onFavorite.emit(this.game);
  }
}