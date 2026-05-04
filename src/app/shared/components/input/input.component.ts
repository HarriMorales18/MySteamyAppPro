import { Component, Output, EventEmitter } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-input',
  templateUrl: './input.component.html',
  styleUrls: ['./input.component.scss'],
})
export class InputComponent {
  @Output() onSearch = new EventEmitter<string>();

  handleInput(event: any) {
    this.onSearch.emit(event.target.value);
  }
}