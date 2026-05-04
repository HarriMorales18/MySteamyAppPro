import { Component } from '@angular/core';
import { Preferences } from '@capacitor/preferences';

@Component({
  standalone: false,
  selector: 'app-favorite',
  templateUrl: './favorite.page.html',
  styleUrls: ['./favorite.page.scss'],
})
export class FavoritePage {
  favoriteGame: any = null;

  constructor() {}

  ionViewWillEnter() {
    this.loadFavorite();
  }

  async loadFavorite() {
    const { value } = await Preferences.get({ key: 'favoriteGame' });
    if (value) {
      this.favoriteGame = JSON.parse(value);
    }
  }

  async removeFavorite() {
    await Preferences.remove({ key: 'favoriteGame' });
    this.favoriteGame = null;
  }
}